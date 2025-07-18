package cn.cug.sxy.domain.storage.service;

import cn.cug.sxy.domain.reception.adapter.repository.ILogBatchRepository;
import cn.cug.sxy.domain.reception.model.entity.LogBatchEntity;
import cn.cug.sxy.domain.reception.model.valobj.BatchId;
import cn.cug.sxy.domain.reception.model.valobj.BatchStatus;
import cn.cug.sxy.domain.storage.model.valobj.StorageLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @version 1.0
 * @Date 2025/7/9 14:06
 * @Description 日志存储服务实现
 * @Author jerryhotton
 */

@Slf4j
@Service
public class LogStorageService implements ILogStorageService {

    private final ILogBatchRepository logBatchRepository;

    public LogStorageService(ILogBatchRepository logBatchRepository) {
        this.logBatchRepository = logBatchRepository;
    }

    @Override
    public String storeBatch(LogBatchEntity batch, List<StorageLog> logs) {
        return logBatchRepository.storeBatch(batch, logs);
    }

    @Override
    public boolean isBatchStored(BatchId batchId) {
        return false;
    }

    @Override
    public BatchStatus queryBatchStatus(BatchId batchId) {
        return null;
    }

    @Override
    public boolean storeLog(StorageLog log) {
        return false;
    }

}
