package cn.cug.sxy.domain.reception.service;

import cn.cug.sxy.domain.reception.adapter.repository.IReceiverEndpointRepository;
import cn.cug.sxy.domain.reception.model.entity.ReceptionDynamicContext;
import cn.cug.sxy.domain.reception.model.valobj.*;
import cn.cug.sxy.domain.reception.service.buffer.LogBufferManager;
import cn.cug.sxy.types.framework.chain.ILogicChain;
import cn.cug.sxy.domain.reception.service.rule.chain.factory.DefaultLogicChainFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @version 1.0
 * @Date 2025/7/7 13:56
 * @Description 日志接收服务实现
 * @Author jerryhotton
 */

@Slf4j
@Service
public class LogReceptionService implements ILogReceptionService, InitializingBean {

    private final IReceiverEndpointRepository receiverEndpointRepository;
    private final DefaultLogicChainFactory logicChainFactory;
    private final LogBufferManager logBufferManager;
    private final IBatchProcessingService batchProcessingService;

    // 异步处理线程池
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

    public LogReceptionService(
            IReceiverEndpointRepository receiverEndpointRepository,
            DefaultLogicChainFactory logicChainFactory,
            LogBufferManager logBufferManager,
            IBatchProcessingService batchProcessingService) {
        this.receiverEndpointRepository = receiverEndpointRepository;
        this.logicChainFactory = logicChainFactory;
        this.logBufferManager = logBufferManager;
        this.batchProcessingService = batchProcessingService;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        logBufferManager.setBatchProcessCallback(batchId ->
                CompletableFuture.runAsync(() -> batchProcessingService.processBatchAsync(batchId), asyncExecutor)
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public ReceptionResult receiveLog(RawLog rawLog, String appId, String endpointId) {
        ILogicChain<ReceptionRequest, ReceptionResult, ReceptionDynamicContext> logicChain =
                (ILogicChain<ReceptionRequest, ReceptionResult, ReceptionDynamicContext>)
                        logicChainFactory.openLogicChain(DefaultLogicChainFactory.RuleType.RECEPTION_SINGLETON_RULE.getCode());
        ReceptionRequest request = ReceptionRequest.builder()
                .rawLog(Collections.singletonList(rawLog))
                .appId(appId)
                .endpointId(endpointId)
                .build();

        return logicChain.logic(request, new ReceptionDynamicContext());
    }

    @Override
    @SuppressWarnings("unchecked")
    public ReceptionResult receiveBatch(List<RawLog> rawLogs, String appId, String endpointId) {
        ILogicChain<ReceptionRequest, ReceptionResult, ReceptionDynamicContext> logicChain =
                (ILogicChain<ReceptionRequest, ReceptionResult, ReceptionDynamicContext>)
                        logicChainFactory.openLogicChain(DefaultLogicChainFactory.RuleType.RECEPTION_BATCH_RULE.getCode());
        ReceptionRequest request = ReceptionRequest.builder()
                .rawLog(rawLogs)
                .appId(appId)
                .endpointId(endpointId)
                .build();

        return logicChain.logic(request, new ReceptionDynamicContext());
    }

    @Override
    public ProcessResult processBatch(BatchId batchId) {
        return null;
    }

    @Override
    public BatchStatus queryBatchStatus(BatchId batchId) {
        return null;
    }

}
