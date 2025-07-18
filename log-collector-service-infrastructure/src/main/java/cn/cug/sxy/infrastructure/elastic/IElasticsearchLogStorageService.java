package cn.cug.sxy.infrastructure.elastic;

import cn.cug.sxy.domain.reception.model.valobj.BatchId;
import cn.cug.sxy.domain.reception.model.valobj.BatchStatus;
import cn.cug.sxy.domain.reception.model.valobj.RawLog;
import cn.cug.sxy.domain.storage.model.valobj.StorageLog;

import java.util.List;

/**
 * @version 1.0
 * @Date 2025/7/11 15:21
 * @Description Elasticsearch日志存储服务接口
 * @Author jerryhotton
 */

public interface IElasticsearchLogStorageService {

    /**
     * 存储日志批次
     *
     * @param batchId 批次ID
     * @param logs    日志列表
     * @return 批次跟踪ID
     */
    String storeBatch(BatchId batchId, List<StorageLog> logs);

    /**
     * 确认批次是否已存储
     *
     * @param batchId 批次ID
     * @return 是否已存储
     */
    boolean isBatchStored(BatchId batchId);

    /**
     * 查询批次存储状态
     *
     * @param batchId 批次ID
     * @return 批次存储状态
     */
    BatchStatus queryBatchStatus(BatchId batchId);

    /**
     * 存储单条日志
     *
     * @param storageLog 日志
     * @return 是否存储成功
     */
    boolean storeLog(StorageLog storageLog);

    List<RawLog> loadLogsForBatch(String batchId, String appId, String endpointId);

}
