package cn.cug.sxy.domain.reception.service;

import cn.cug.sxy.domain.reception.adapter.repository.ILogBatchRepository;
import cn.cug.sxy.domain.reception.alc.IPreprocessGateway;
import cn.cug.sxy.domain.reception.alc.IStorageGateway;
import cn.cug.sxy.domain.reception.model.entity.LogBatchEntity;
import cn.cug.sxy.domain.reception.model.valobj.BatchId;
import cn.cug.sxy.domain.reception.model.valobj.BatchStatus;
import cn.cug.sxy.domain.reception.model.valobj.ProcessedLog;
import cn.cug.sxy.domain.reception.service.metrics.LogProcessingMetrics;
import cn.cug.sxy.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @version 1.0
 * @Date 2025/7/9 16:47
 * @Description 批次处理服务实现
 * @Author jerryhotton
 */

@Slf4j
@Service
public class BatchProcessingService implements IBatchProcessingService {

    private final ILogBatchRepository logBatchRepository;
    private final IStorageGateway storageGateway;
    private final IPreprocessGateway preprocessGateway;
    private final LogProcessingMetrics metrics;

    // 重试配置
    private static final int MAX_RETRY_COUNT = 3;
    private static final long RETRY_INTERVAL_MS = 1000;

    public BatchProcessingService(
            ILogBatchRepository logBatchRepository,
            IStorageGateway storageGateway,
            IPreprocessGateway preprocessGateway,
            LogProcessingMetrics metrics) {
        this.logBatchRepository = logBatchRepository;
        this.storageGateway = storageGateway;
        this.preprocessGateway = preprocessGateway;
        this.metrics = metrics;
    }

    @Override
    public void processBatchAsync(LogBatchEntity batchEntity) {
        if (batchEntity == null) {
            log.error("批次为空，无法处理");
            return;
        }
        BatchId batchId = batchEntity.getId();
        int retryCount = 0;
        boolean success = false;
        Exception lastException = null;
        while (retryCount <= MAX_RETRY_COUNT && !success) {
            try {
                // 1. 检查批次是否存在并获取批次信息 (只读事务)
                LogBatchEntity batch = fetchAndValidateBatch(batchEntity);
                if (batch == null) {
                    return; // 批次不存在或已处理完成
                }
                // 2. 处理批次 (每个步骤都有自己的事务)
                success = processLogBatch(batch, retryCount);
                // 如果处理失败但没有抛出异常，记录失败并准备重试
                if (!success) {
                    retryCount++;
                    log.warn("批次处理失败，准备第{}次重试: batchId={}",
                            retryCount, batchId.getValue());
                    // 执行重试等待策略
                    if (retryCount <= MAX_RETRY_COUNT && !executeRetryWait(retryCount, batchId)) {
                        break; // 如果等待被中断则退出重试
                    }
                }
            } catch (Exception e) {
                lastException = e;
                retryCount++;
                // 记录异常信息
                log.error("批次处理异常: batchId={}, retryCount={}, error={}",
                        batchId.getValue(), retryCount, e.getMessage(), e);
                // 执行重试等待策略
                if (retryCount <= MAX_RETRY_COUNT && !executeRetryWait(retryCount, batchId)) {
                    break; // 如果等待被中断则退出重试
                }
            }
        }
        // 如果重试后仍然失败，标记批次为最终失败 (独立事务)
        if (!success && retryCount > MAX_RETRY_COUNT) {
            BatchProcessingService proxy = (BatchProcessingService) AopContext.currentProxy();
            proxy.handleFinalFailure(batchId, lastException);
        }
    }

    /**
     * 获取并校验批次
     *
     * @param logBatchEntity 批次
     * @return 批次对象，如果批次不存在或已经处理完成则返回null
     */
    public LogBatchEntity fetchAndValidateBatch(LogBatchEntity logBatchEntity) {
        // 查询批次
        BatchId batchId = logBatchEntity.getId();
        Optional<LogBatchEntity> batchOpt = logBatchRepository.findByBatchId(batchId);
        if (!batchOpt.isPresent()) {
            log.error("异步处理批次失败: 批次不存在, batchId={}", batchId.getValue());
            return null;
        }
        LogBatchEntity batch = batchOpt.get();
        batch.setLogs(logBatchEntity.getLogs());
        // 幂等性检查 - 如果批次已经处理完成，直接返回成功
        if (batch.getStatus() == BatchStatus.PROCESSED) {
            log.info("批次已处理，跳过: batchId={}", batchId.getValue());
            return null;
        }
        // 如果批次已经失败且已达到最大重试次数，不再处理
        if (batch.getStatus() == BatchStatus.FAILED && batch.getRetryCount() >= MAX_RETRY_COUNT) {
            log.warn("批次处理失败且已达到最大重试次数，不再处理: batchId={}, retryCount={}",
                    batchId.getValue(), batch.getRetryCount());
            return null;
        }
        // 检查批次是否已经被存储领域处理（幂等性检查）
        if (storageGateway.isBatchStored(batchId)) {
            log.info("批次已被存储领域处理，标记为已处理: batchId={}", batchId.getValue());
            try {
                // 使用独立事务更新状态，确保本次检查与更新的数据一致性
                BatchProcessingService proxy = (BatchProcessingService) AopContext.currentProxy();
                proxy.updateBatchAsAlreadyStored(batchId);
            } catch (Exception e) {
                log.error("更新批次状态失败: batchId={}, error={}", batchId.getValue(), e.getMessage(), e);
                // 即使更新失败，也认为批次已处理，防止重复处理
            }
            return null;
        }

        return batch;
    }

    /**
     * 标记批次为已存储状态 (独立事务)
     *
     * @param batchId 批次ID
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateBatchAsAlreadyStored(BatchId batchId) {
        Instant now = Instant.now();
        logBatchRepository.updateProcessedBatchStatus(
                batchId,
                BatchStatus.PROCESSED,
                now,
                now,
                null,  // 无需跟踪ID，因为只是确认已存储
                true   // 已确认
        );
    }

    /**
     * 处理日志批次
     *
     * @param batch      日志批次
     * @param retryCount 当前重试次数
     * @return 是否处理成功
     */
    private boolean processLogBatch(LogBatchEntity batch, int retryCount) {
        BatchId batchId = batch.getId();
        try {
            // 1. 更新批次状态为处理中 (独立事务)
            BatchProcessingService proxy = (BatchProcessingService) AopContext.currentProxy();
            proxy.updateBatchToProcessing(batchId, retryCount);
            // 2. 预处理和验证日志 (非事务操作，纯内存处理)
            List<ProcessedLog> validLogs = preprocessAndValidateLogs(batch);
            if (validLogs.isEmpty()) {
                // 没有有效日志，直接标记为处理完成 (独立事务)
                proxy.markBatchAsProcessed(batchId);
                return true;
            }
            // 3. 将日志批次传递给下游存储领域 (非事务操作，外部系统调用)
            IStorageGateway.BatchStorageResult storageResult = storeLogBatch(batch, validLogs);
            // 4. 处理完成后更新批次状态 (独立事务)
            proxy.updateProcessedBatchStatus(batchId, storageResult.getBatchTraceId());
            // 5. 发布事件并记录度量指标 (非事务操作或独立事务)
            publishSuccessEventAndMetrics(batchId, validLogs.size(), storageResult.getBatchTraceId());

            return true;
        } catch (Exception e) {
            // 任何处理过程中的异常都记录并向上抛出，由上层重试机制处理
            log.error("处理批次过程中出错: batchId={}, error={}", batchId.getValue(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 更新批次状态为处理中
     *
     * @param batchId    批次ID
     * @param retryCount 重试次数
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateBatchToProcessing(BatchId batchId, int retryCount) {
        try {
            Instant now = Instant.now();
            logBatchRepository.updateStatusAndRetryCount(
                    batchId,
                    BatchStatus.PROCESSING,
                    retryCount,
                    now
            );
        } catch (Exception e) {
            log.error("更新批次状态为处理中失败: batchId={}, retryCount={}, error={}",
                    batchId.getValue(), retryCount, e.getMessage(), e);
            throw new AppException("更新批次状态失败", e);
        }
    }

    /**
     * 预处理和验证日志
     *
     * @param batch 日志批次
     * @return 处理后的有效日志列表，如果没有有效日志则返回空列表
     */
    private List<ProcessedLog> preprocessAndValidateLogs(LogBatchEntity batch) {
        try {
            // 1. 日志预处理 - 格式转换、字段提取等
            List<ProcessedLog> processedLogs = preprocessLogs(batch);
            // 2. todo 日志验证 - 应用业务规则链

            // 过滤出验证通过的日志
            List<ProcessedLog> validLogs = processedLogs.stream()
                    .filter(ProcessedLog::isValidated)
                    .collect(Collectors.toList());
            if (validLogs.isEmpty()) {
                log.warn("批次中没有有效日志，标记为处理完成: batchId={}", batch.getId().getValue());
                return Collections.emptyList();
            }

            return validLogs;
        } catch (Exception e) {
            log.error("预处理和验证日志失败: batchId={}, error={}",
                    batch.getId().getValue(), e.getMessage(), e);
            throw new AppException("预处理和验证日志失败", e);
        }
    }

    /**
     * 存储日志批次到下游领域
     *
     * @param batch     日志批次
     * @param validLogs 有效的日志列表
     * @return 存储结果
     */
    private IStorageGateway.BatchStorageResult storeLogBatch(LogBatchEntity batch, List<ProcessedLog> validLogs) {
        try {
            // 调用存储适配器存储批次
            IStorageGateway.BatchStorageResult storageResult = storageGateway.storeBatch(batch, validLogs);
            if (!storageResult.isSuccess()) {
                throw new AppException("批次传输到存储领域失败: " + storageResult.getErrorMessage());
            }

            return storageResult;
        } catch (Exception e) {
            log.error("存储批次失败: batchId={}, error={}", batch.getId().getValue(), e.getMessage(), e);
            throw new AppException("存储批次失败", e);
        }
    }

    /**
     * 标记批次为已处理状态
     *
     * @param batchId 批次ID
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markBatchAsProcessed(BatchId batchId) {
        try {
            Instant now = Instant.now();
            logBatchRepository.updateStatusAndProcessTime(
                    batchId,
                    BatchStatus.PROCESSED,
                    now,
                    now
            );
        } catch (Exception e) {
            log.error("标记批次为已处理状态失败: batchId={}, error={}", batchId.getValue(), e.getMessage(), e);
            throw new AppException("标记批次为已处理状态失败", e);
        }
    }

    /**
     * 更新已处理批次的状态和相关信息
     *
     * @param batchId      批次ID
     * @param batchTraceId 批次跟踪ID
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateProcessedBatchStatus(BatchId batchId, String batchTraceId) {
        try {
            Instant now = Instant.now();
            logBatchRepository.updateProcessedBatchStatus(
                    batchId,
                    BatchStatus.PROCESSED,
                    now,
                    now,
                    batchTraceId,
                    true
            );
        } catch (Exception e) {
            log.error("更新已处理批次状态失败: batchId={}, error={}", batchId.getValue(), e.getMessage(), e);
            throw new AppException("更新已处理批次状态失败", e);
        }
    }

    /**
     * 发布成功事件和记录度量指标
     *
     * @param batchId       批次ID
     * @param validLogCount 有效日志数量
     * @param batchTraceId  批次跟踪ID
     */
    public void publishSuccessEventAndMetrics(BatchId batchId, int validLogCount, String batchTraceId) {
        try {
            // 重新获取完整实体用于事件发布和指标记录
            Optional<LogBatchEntity> batchOpt = logBatchRepository.findByBatchId(batchId);
            if (batchOpt.isPresent()) {
                LogBatchEntity batch = batchOpt.get();

                // todo 发布批次处理完成事件 (异步操作，不影响事务)

                // 记录监控指标 (非事务操作)
                metrics.recordProcessSuccess(batch.getAppId(), batch.getEndpointId(),
                        validLogCount, batch.getProcessingTimeMs());

                log.info("批次处理完成: batchId={}, logCount={}, validCount={}, batchTraceId={}, processingTimeMs={}",
                        batchId.getValue(), batch.getLogCount(), validLogCount,
                        batchTraceId, batch.getProcessingTimeMs());
            } else {
                log.warn("无法读取批次信息以发布事件和指标: batchId={}", batchId.getValue());
            }
        } catch (Exception e) {
            // 事件发布失败不应影响批次处理结果，仅记录日志
            log.error("发布成功事件或记录指标失败: batchId={}, error={}", batchId.getValue(), e.getMessage(), e);
        }
    }

    /**
     * 执行重试等待策略
     *
     * @param retryCount 当前重试次数
     * @param batchId    批次ID
     * @return 是否成功等待（未被中断）
     */
    private boolean executeRetryWait(int retryCount, BatchId batchId) {
        // 记录重试指标
        try {
            recordRetryMetrics(batchId);
        } catch (Exception e) {
            log.warn("记录重试指标失败: batchId={}, error={}", batchId.getValue(), e.getMessage());
        }

        try {
            // 指数退避策略，每次重试等待时间递增
            long waitTime = RETRY_INTERVAL_MS * (long) Math.pow(2, retryCount - 1);
            Thread.sleep(waitTime);
            return true;
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("批次处理重试等待被中断: batchId={}", batchId.getValue());
            return false;
        }
    }

    /**
     * 记录重试指标
     *
     * @param batchId 批次ID
     */
    public void recordRetryMetrics(BatchId batchId) {
        Optional<LogBatchEntity> batchOpt = logBatchRepository.findByBatchId(batchId);
        if (batchOpt.isPresent()) {
            metrics.recordRetry(batchOpt.get().getAppId(), batchOpt.get().getEndpointId());
        }
    }

    /**
     * 处理最终失败的情况
     *
     * @param batchId   批次ID
     * @param exception 导致失败的异常
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleFinalFailure(BatchId batchId, Exception exception) {
        try {
            String errorMessage = "批次处理失败，已达到最大重试次数: " + exception.getMessage();
            log.error(errorMessage);
            // 更新批次状态为失败
            logBatchRepository.updateStatus(batchId, BatchStatus.FAILED, errorMessage);
            // 记录失败指标和通知操作在单独的事务中执行
            recordFailureAndNotify(batchId, exception);
        } catch (Exception e) {
            log.error("处理最终失败状态时出错: batchId={}, error={}", batchId.getValue(), e.getMessage(), e);
        }
    }

    /**
     * 记录失败指标并发送通知
     *
     * @param batchId   批次ID
     * @param exception 异常
     */
    public void recordFailureAndNotify(BatchId batchId, Exception exception) {
        try {
            // 重新获取完整实体用于事件发布和指标记录
            Optional<LogBatchEntity> batchOpt = logBatchRepository.findByBatchId(batchId);
            if (batchOpt.isPresent()) {
                LogBatchEntity batch = batchOpt.get();
                // 记录失败指标
                metrics.recordProcessFailure(batch.getAppId(), batch.getEndpointId());
                // 通知处理失败
                notifyProcessingFailure(batch, exception);
            } else {
                log.warn("无法读取批次信息以记录失败: batchId={}", batchId.getValue());
            }
        } catch (Exception e) {
            log.error("记录失败指标和发送通知时出错: batchId={}, error={}", batchId.getValue(), e.getMessage(), e);
        }
    }

    /**
     * 预处理日志批次中的原始日志
     *
     * @param batch 日志批次
     * @return 处理后的日志列表
     */
    private List<ProcessedLog> preprocessLogs(LogBatchEntity batch) {
        // 通过预处理适配器处理日志批次
        return preprocessGateway.preprocessFromBatch(batch);
    }

    /**
     * 通知处理失败
     *
     * @param batch     日志批次
     * @param exception 异常信息
     */
    private void notifyProcessingFailure(LogBatchEntity batch, Exception exception) {
        // TODO: 实现告警通知逻辑，如发送邮件、短信、钉钉等
        log.error("需要人工干预的批次处理失败: batchId={}, appId={}, endpointId={}, error={}",
                batch.getId().getValue(),
                batch.getAppId(),
                batch.getEndpointId(),
                exception != null ? exception.getMessage() : "未知错误");
    }

}
