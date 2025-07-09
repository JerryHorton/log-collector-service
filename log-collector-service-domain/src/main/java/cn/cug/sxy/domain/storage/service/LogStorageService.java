package cn.cug.sxy.domain.storage.service;

import cn.cug.sxy.domain.reception.model.valobj.BatchId;
import cn.cug.sxy.domain.reception.model.valobj.BatchStatus;
import cn.cug.sxy.domain.storage.model.valobj.StorageLog;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @version 1.0
 * @Date 2025/7/9 14:06
 * @Description 日志存储服务实现
 * @Author jerryhotton
 */

@Service
public class LogStorageService implements ILogStorageService {

    @Override
    public String storeBatch(BatchId batchId, List<StorageLog> logs) {
        return "";
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
