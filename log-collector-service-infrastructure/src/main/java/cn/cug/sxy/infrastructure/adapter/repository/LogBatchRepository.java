package cn.cug.sxy.infrastructure.adapter.repository;

import cn.cug.sxy.domain.reception.adapter.repository.ILogBatchRepository;
import cn.cug.sxy.domain.reception.model.entity.LogBatchEntity;
import cn.cug.sxy.domain.auth.model.valobj.AppId;
import cn.cug.sxy.domain.reception.model.valobj.BatchId;
import cn.cug.sxy.domain.reception.model.valobj.BatchStatus;
import cn.cug.sxy.domain.reception.model.valobj.LogFormat;
import cn.cug.sxy.domain.reception.model.valobj.RawLog;
import cn.cug.sxy.domain.storage.model.entity.LogDocument;
import cn.cug.sxy.domain.storage.model.valobj.LogQuery;
import cn.cug.sxy.domain.storage.model.valobj.LogQueryResult;
import cn.cug.sxy.domain.storage.model.valobj.StorageLog;
import cn.cug.sxy.infrastructure.dao.ILogBatchDao;
import cn.cug.sxy.infrastructure.dao.po.LogBatch;
import cn.cug.sxy.infrastructure.elastic.IElasticsearchLogQueryService;
import cn.cug.sxy.infrastructure.elastic.IElasticsearchLogStorageService;
import cn.cug.sxy.types.model.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @version 1.0
 * @Date 2025/7/8 19:08
 * @Description 日志批次仓储实现
 * @Author jerryhotton
 */

@Slf4j
@Repository
public class LogBatchRepository extends AbstractRepository implements ILogBatchRepository {

    private final IElasticsearchLogStorageService elasticsearchLogStorageService;
    private final IElasticsearchLogQueryService elasticsearchLogQueryService;
    private final ILogBatchDao logBatchDao;

    public LogBatchRepository(
            IElasticsearchLogStorageService elasticsearchLogStorageService,
            IElasticsearchLogQueryService elasticsearchLogQueryService,
            ILogBatchDao logBatchDao) {
        this.elasticsearchLogStorageService = elasticsearchLogStorageService;
        this.elasticsearchLogQueryService = elasticsearchLogQueryService;
        this.logBatchDao = logBatchDao;
    }

    @Override
    public void save(LogBatchEntity logBatchEntity) {
        LogBatch logBatch = convertToLogBatch(logBatchEntity);
        // 存储日志批次元数据到关系数据库
        logBatchDao.save(logBatch);
    }

    @Override
    public Optional<LogBatchEntity> findByBatchId(BatchId batchId) {
        if (null == batchId) {
            return Optional.empty();
        }
        LogBatch batch = logBatchDao.selectById(batchId.getValue());
        if (null == batch) {
            return Optional.empty();
        }
        return Optional.of(convertToLogBatchEntity(batch));
    }

    @Override
    public List<LogBatchEntity> findByAppIdAndStatus(AppId appId, BatchStatus status) {
        return List.of();
    }

    @Override
    public Page<LogBatchEntity> findByTimeRangeAndStatus(Instant startTime, Instant endTime, BatchStatus status, int pageNumber, int pageSize) {
        return null;
    }

    @Override
    public void updateStatus(BatchId batchId, BatchStatus status, String errorMessage) {

    }

    @Override
    public int deleteExpired(Instant beforeTime) {
        return 0;
    }

    @Override
    public List<LogBatchEntity> findByStatusAndProcessedTimeBefore(BatchStatus status, Instant before) {
        return List.of();
    }

    @Override
    public List<LogBatchEntity> findByStatusAndLastProcessTimeBefore(BatchStatus status, Instant before) {
        return List.of();
    }

    @Override
    public void updateStatusAndProcessTime(BatchId batchId, BatchStatus status, Instant processedTime, Instant lastProcessTime) {

    }

    @Override
    public void updateStatusAndRetryCount(BatchId batchId, BatchStatus status, int retryCount, Instant lastProcessTime) {

    }

    @Override
    public void updateProcessedBatchStatus(BatchId batchId, BatchStatus status, Instant processedTime, Instant lastProcessTime, String batchTraceId, boolean confirmed) {

    }

    @Override
    public void delete(LogBatchEntity batch) {

    }

    @Override
    public List<LogBatchEntity> findAll() {
        return List.of();
    }

    @Override
    public Map<BatchStatus, Long> countByStatus() {
        return Map.of();
    }

    @Override
    public String storeBatch(LogBatchEntity logBatchEntity, List<StorageLog> logs) {
        try {
            // 1. 将日志内容发送到Elasticsearch
            String traceId = elasticsearchLogStorageService.storeBatch(logBatchEntity.getId(), logs);
            // 2. 更新批次状态和跟踪ID
            if (traceId != null) {
                logBatchEntity.markAsProcessed(traceId);
            } else {
                logBatchEntity.markAsFailed("存储到Elasticsearch失败");
            }
            LogBatch logBatch = convertToLogBatch(logBatchEntity);
            logBatchDao.updateBatchMetadata(logBatch);

            return traceId;
        } catch (Exception e) {
            log.error("保存日志批次异常: batchId={}, error={}",
                    logBatchEntity.getId().getValue(), e.getMessage(), e);
            logBatchEntity.markAsFailed("批次保存异常: " + e.getMessage());
            LogBatch logBatch = convertToLogBatch(logBatchEntity);
            logBatchDao.updateBatchMetadata(logBatch);

            return null;
        }
    }

    @Override
    public LogQueryResult queryLogs(LogQuery query) {
        return elasticsearchLogQueryService.queryLogs(query);
    }

    @Override
    public LogDocument getLogById(String logId) {
        return elasticsearchLogQueryService.getLogById(logId);
    }

    // 将LogBatch转换为StorageLog列表
    private List<StorageLog> convertToStorageLogs(LogBatchEntity batch) {
        return batch.getLogs().stream()
                .map(rawLog -> {
                    // 准备元数据
                    Map<String, String> metadata = new HashMap<>(rawLog.getMetadata());
                    metadata.put("appId", batch.getAppId());
                    metadata.put("endpointId", batch.getEndpointId());
                    metadata.put("format", rawLog.getFormat().name());
                    metadata.put("sourceId", rawLog.getSourceId());
                    metadata.put("timestamp", String.valueOf(
                            rawLog.getTimestamp().toEpochMilli()));
                    // 准备结构化字段（如果内容是JSON）
                    Map<String, Object> structuredFields = new HashMap<>();
                    if (rawLog.getFormat() == LogFormat.JSON) {
                        try {
                            // 使用Jackson解析JSON内容
                            if (rawLog.getContent() != null && !rawLog.getContent().isEmpty()) {
                                ObjectMapper objectMapper = new ObjectMapper();
                                // 启用特性以处理不同类型的JSON结构
                                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                                objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
                                try {
                                    // 尝试解析为Map结构
                                    structuredFields = objectMapper.readValue(rawLog.getContent(),
                                            new TypeReference<>() {
                                            });
                                    // 确保所有值都是可序列化的
                                    structuredFields = sanitizeMap(structuredFields);
                                } catch (JsonProcessingException e) {
                                    log.warn("无法将日志内容解析为JSON: batchId={}, error={}",
                                            batch.getId().getValue(), e.getMessage());
                                    // 创建部分结构化数据，包含原始内容和解析错误
                                    structuredFields = new HashMap<>();
                                    structuredFields.put("_raw_content", rawLog.getContent());
                                    structuredFields.put("_parse_error", e.getMessage());
                                }
                            }
                        } catch (Exception e) {
                            log.warn("解析JSON日志失败: {}", e.getMessage());
                        }
                    }

                    return new StorageLog(rawLog.getContent(), metadata, structuredFields);
                })
                .collect(Collectors.toList());
    }

    private LogBatch convertToLogBatch(LogBatchEntity logBatchEntity) {
        LogBatch logBatch = new LogBatch();
        // 设置基本属性
        logBatch.setBatchId(logBatchEntity.getId().getValue());
        logBatch.setAppId(logBatchEntity.getAppId());
        logBatch.setEndpointId(logBatchEntity.getEndpointId());
        logBatch.setStatus(logBatchEntity.getStatus().name());
        logBatch.setErrorMessage(logBatchEntity.getErrorMessage());
        logBatch.setBatchTraceId(logBatchEntity.getBatchTraceId());
        logBatch.setConfirmed(logBatchEntity.getConfirmed());
        // 设置批次信息
        logBatch.setLogCount(logBatchEntity.getLogs().size());
        logBatch.setPayloadSize(calculatePayloadSize(logBatchEntity.getLogs()));
        logBatch.setRetryCount(logBatchEntity.getRetryCount());
        logBatch.setPriority(logBatchEntity.getPriority());
        // 设置时间信息
        logBatch.setReceivedTime(Date.from(logBatchEntity.getReceivedTime()));
        if (logBatchEntity.getProcessedTime() != null) {
            logBatch.setProcessedTime(Date.from(logBatchEntity.getProcessedTime()));
        }
        if (logBatchEntity.getLastProcessTime() != null) {
            logBatch.setLastProcessTime(Date.from(logBatchEntity.getLastProcessTime()));
        }
        // 设置元数据
        if (!logBatchEntity.getLogs().isEmpty()) {
            logBatch.setFormat(determineMainFormat(logBatchEntity.getLogs()));
            logBatch.setSource(determineMainSource(logBatchEntity.getLogs()));
        }
        // 设置存储信息
        logBatch.setStorageType("elasticsearch"); // 默认使用ES
        logBatch.setStorageIndex(getIndexName()); // 获取当前索引名
        // 设置系统时间
        logBatch.setCreatedTime(new Date());
        logBatch.setUpdatedTime(new Date());

        return logBatch;
    }

    /**
     * 计算批次总负载大小
     */
    private long calculatePayloadSize(List<RawLog> logs) {
        return logs.stream()
                .mapToLong(log -> log.getContent().getBytes().length)
                .sum();
    }

    /**
     * 确定批次主要日志格式
     */
    private String determineMainFormat(List<RawLog> logs) {
        // 简单实现：返回第一条日志的格式
        if (!logs.isEmpty()) {
            return logs.get(0).getFormat().name();
        }
        return "UNKNOWN";
    }

    /**
     * 确定批次主要来源
     */
    private String determineMainSource(List<RawLog> logs) {
        // 简单实现：返回第一条日志的来源
        if (!logs.isEmpty()) {
            return logs.get(0).getMetadata().getOrDefault("source", "unknown");
        }
        return "unknown";
    }

    /**
     * 获取当前索引名
     */
    private String getIndexName() {
        return "logs-" + java.time.LocalDate.now().toString().replace("-", ".");
    }

    /**
     * 清理Map，确保所有值都是ES可接受的类型
     */
    private Map<String, Object> sanitizeMap(Map<String, Object> map) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value == null) {
                result.put(key, null);
            } else if (value instanceof Map) {
                // 递归处理嵌套Map
                result.put(key, sanitizeMap((Map<String, Object>) value));
            } else if (value instanceof Collection) {
                // 处理集合类型
                result.put(key, sanitizeCollection((Collection<?>) value));
            } else if (value instanceof Number || value instanceof Boolean ||
                    value instanceof String || value instanceof Date) {
                // 基础类型直接添加
                result.put(key, value);
            } else {
                // 其他类型转为字符串
                result.put(key, value.toString());
            }
        }
        return result;
    }

    /**
     * 清理集合，确保所有元素都是ES可接受的类型
     */
    private List<Object> sanitizeCollection(Collection<?> collection) {
        List<Object> result = new ArrayList<>();
        for (Object item : collection) {
            if (item == null) {
                result.add(null);
            } else if (item instanceof Map) {
                result.add(sanitizeMap((Map<String, Object>) item));
            } else if (item instanceof Collection) {
                result.add(sanitizeCollection((Collection<?>) item));
            } else if (item instanceof Number || item instanceof Boolean ||
                    item instanceof String || item instanceof Date) {
                result.add(item);
            } else {
                result.add(item.toString());
            }
        }
        return result;
    }

    /**
     * 将持久层对象 LogBatch 转换为领域实体 LogBatchEntity
     *
     * @param logBatch 日志批次持久层对象
     * @return 日志批次领域实体
     */
    private LogBatchEntity convertToLogBatchEntity(LogBatch logBatch) {
        // 1. 创建BatchId值对象
        BatchId batchId = new BatchId(logBatch.getBatchId());
        // 2. 创建空的原始日志列表 - 注意：持久层对象中没有保存日志内容
        List<RawLog> logs = new ArrayList<>();
        // 3. 转换批次状态
        BatchStatus status = BatchStatus.valueOf(logBatch.getStatus());
        // 4. 转换时间对象
        Instant receivedTime = logBatch.getReceivedTime().toInstant();
        Instant processedTime = logBatch.getProcessedTime() != null ?
                logBatch.getProcessedTime().toInstant() : null;
        Instant lastProcessTime = logBatch.getLastProcessTime() != null ?
                logBatch.getLastProcessTime().toInstant() : null;
        // 5. 创建领域实体对象
        LogBatchEntity logBatchEntity = new LogBatchEntity(
                batchId,
                logBatch.getAppId(),
                logBatch.getEndpointId(),
                logs,  // 空列表或需从其他存储加载
                status,
                receivedTime
        );
        // 6. 设置其他属性
        logBatchEntity.setErrorMessage(logBatch.getErrorMessage());
        logBatchEntity.setBatchTraceId(logBatch.getBatchTraceId());
        logBatchEntity.setConfirmed(logBatch.isConfirmed());
        logBatchEntity.setRetryCount(logBatch.getRetryCount());
        logBatchEntity.setPriority(logBatch.getPriority());
        logBatchEntity.setProcessedTime(processedTime);
        logBatchEntity.setLastProcessTime(lastProcessTime);

        return logBatchEntity;
    }

    /**
     * 从存储中加载批次的日志内容
     * 这是一个可选方法，如果需要完整的日志内容则调用此方法
     *
     * @param logBatchEntity 基本信息已填充的实体
     * @return 加载了日志内容的实体
     */
    private LogBatchEntity loadLogsForBatch(LogBatchEntity logBatchEntity) {
        // 从ES中加载该批次的日志
        String batchId = logBatchEntity.getId().getValue();
        String appId = logBatchEntity.getAppId();
        String endpointId = logBatchEntity.getEndpointId();
        try {
            // 查询ES获取批次日志
            List<RawLog> logs = elasticsearchLogStorageService.loadLogsForBatch(
                    batchId, appId, endpointId);
            // 将日志内容设置到实体中
            if (logs != null && !logs.isEmpty()) {
                logBatchEntity.setLogs(logs);
            }
        } catch (Exception e) {
            log.error("从ES加载批次日志异常: batchId={}, error={}",
                    batchId, e.getMessage(), e);
        }

        return logBatchEntity;
    }

    /**
     * 根据批次ID查找并转换为领域实体
     *
     * @param batchId  批次ID
     * @param loadLogs 是否加载完整日志内容
     * @return 批次实体，未找到则返回空
     */
    public Optional<LogBatchEntity> findByBatchId(String batchId, boolean loadLogs) {
        // 查询数据库获取元数据
        LogBatch batch = logBatchDao.selectById(batchId);
        if (null == batch) {
            return Optional.empty();
        }
        // 转换为领域实体
        LogBatchEntity entity = convertToLogBatchEntity(batch);
        // 如果需要，加载完整日志内容
        if (loadLogs) {
            entity = loadLogsForBatch(entity);
        }

        return Optional.of(entity);
    }

}
