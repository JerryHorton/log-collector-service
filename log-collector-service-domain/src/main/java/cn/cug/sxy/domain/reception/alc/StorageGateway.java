package cn.cug.sxy.domain.reception.alc;

import cn.cug.sxy.domain.reception.model.entity.LogBatch;
import cn.cug.sxy.domain.reception.model.valobj.BatchId;
import cn.cug.sxy.domain.reception.model.valobj.ProcessedLog;
import cn.cug.sxy.domain.storage.service.ILogStorageService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @version 1.0
 * @Date 2025/7/9 14:00
 * @Description 日志存储领域防腐层实现
 * @Author jerryhotton
 */

@Component
public class StorageGateway implements IStorageGateway {

    private final ILogStorageService logStorageService;

    public StorageGateway(ILogStorageService logStorageService) {
        this.logStorageService = logStorageService;
    }

    @Override
    public BatchStorageResult storeBatch(LogBatch batch, List<ProcessedLog> processedLogs) {
        return null;
    }

    @Override
    public CompletableFuture<BatchStorageResult> storeBatchAsync(LogBatch batch, List<ProcessedLog> processedLogs) {
        return null;
    }

    @Override
    public boolean isBatchStored(BatchId batchId) {
        return false;
    }

    @Override
    public BatchStorageStatus queryBatchStatus(BatchId batchId) {
        return null;
    }
}
