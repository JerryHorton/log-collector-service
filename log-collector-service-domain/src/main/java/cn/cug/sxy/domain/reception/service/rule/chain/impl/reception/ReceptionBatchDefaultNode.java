package cn.cug.sxy.domain.reception.service.rule.chain.impl.reception;

import cn.cug.sxy.domain.reception.adapter.repository.ILogBatchRepository;
import cn.cug.sxy.domain.reception.model.entity.LogBatch;
import cn.cug.sxy.domain.reception.model.entity.ReceptionDynamicContext;
import cn.cug.sxy.domain.reception.model.valobj.*;
import cn.cug.sxy.domain.reception.service.IBatchProcessingService;
import cn.cug.sxy.domain.reception.service.buffer.LogBufferManager;
import cn.cug.sxy.domain.reception.service.metrics.LogProcessingMetrics;
import cn.cug.sxy.domain.reception.service.rule.chain.AbstractLogicChainNode;
import cn.cug.sxy.domain.reception.service.rule.chain.factory.DefaultLogicChainFactory;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @version 1.0
 * @Date 2025/7/9 09:36
 * @Description 默认节点（批次接收）
 * @Author jerryhotton
 */

@Slf4j
@Component(value = "reception_batch_default_node")
public class ReceptionBatchDefaultNode extends AbstractLogicChainNode<ReceptionRequest, ReceptionResult, ReceptionDynamicContext> {

    private final ILogBatchRepository logBatchRepository;
    private final LogProcessingMetrics metrics;
    private final IBatchProcessingService batchProcessingService;

    private final Executor asyncExecutor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
            new ThreadFactory() {
                private final AtomicInteger counter = new AtomicInteger();

                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setName("log-batch-processor-" + counter.incrementAndGet());
                    thread.setDaemon(true);
                    return thread;
                }
            }
    );

    public ReceptionBatchDefaultNode(
            ILogBatchRepository logBatchRepository,
            LogProcessingMetrics metrics,
            IBatchProcessingService batchProcessingService) {
        this.logBatchRepository = logBatchRepository;
        this.metrics = metrics;
        this.batchProcessingService = batchProcessingService;
    }

    @Override
    public ReceptionResult logic(ReceptionRequest request, ReceptionDynamicContext context) {
        AppId appId = request.getAppId();
        EndpointId endpointId = request.getEndpointId();
        List<RawLog> validLogs = context.getValidLogs();
        List<RawLog> invalidLogs = context.getInvalidLogs();
        log.info("日志接收责任链-默认节点接管 request:{}", JSON.toJSONString(request));
        // 1. 创建批次ID
        BatchId batchId = BatchId.generate();
        // 2. 创建日志批次
        LogBatch batch = new LogBatch(
                batchId,
                appId,
                endpointId,
                validLogs,
                BatchStatus.PENDING,
                Instant.now()
        );
        // 3. 保存批次
        logBatchRepository.save(batch);
        // 4. 记录监控指标
        metrics.recordBatchReceived(appId, endpointId);
        metrics.recordLogReceived(appId, endpointId, validLogs.size());
        // 5. 异步处理批次
        CompletableFuture.runAsync(() -> batchProcessingService.processBatchAsync(batchId), asyncExecutor);
        log.info("批量日志接收成功: appId={}, endpointId={}, batchId={}, validCount={}, invalidCount={}",
                appId.getValue(), endpointId.getValue(), batchId.getValue(), validLogs.size(), invalidLogs.size());
        log.info("日志接收责任链-默认节点放行 request:{}", JSON.toJSONString(request));

        return ReceptionResult.success(batchId);
    }

    @Override
    protected String ruleNode() {
        return DefaultLogicChainFactory.NodeType.RECEPTION_SINGLETON_DEFAULT_NODE.getCode();
    }

}
