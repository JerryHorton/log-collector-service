package cn.cug.sxy.domain.reception.alc;

import cn.cug.sxy.domain.reception.model.entity.LogBatch;
import cn.cug.sxy.domain.reception.model.valobj.BatchId;
import cn.cug.sxy.domain.reception.model.valobj.ProcessedLog;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @version 1.0
 * @Date 2025/7/9 11:53
 * @Description 日志存储领域防腐层接口
 * @Author jerryhotton
 */

public interface IStorageGateway {

    /**
     * 存储日志批次
     *
     * @param batch 日志批次
     * @param processedLogs 处理后的日志列表
     * @return 存储结果，包含批次跟踪ID
     */
    BatchStorageResult storeBatch(LogBatch batch, List<ProcessedLog> processedLogs);

    /**
     * 异步存储日志批次
     *
     * @param batch 日志批次
     * @param processedLogs 处理后的日志列表
     * @return 异步存储结果
     */
    CompletableFuture<BatchStorageResult> storeBatchAsync(LogBatch batch, List<ProcessedLog> processedLogs);

    /**
     * 确认批次是否已存储
     * 用于幂等性检查
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
    BatchStorageStatus queryBatchStatus(BatchId batchId);

    /**
     * 批次存储结果
     */
    @Getter
    class BatchStorageResult {
        private final boolean success;
        private final String batchTraceId;
        private final String errorMessage;

        public static BatchStorageResult success(String batchTraceId) {
            return new BatchStorageResult(true, batchTraceId, null);
        }

        public static BatchStorageResult failure(String errorMessage) {
            return new BatchStorageResult(false, null, errorMessage);
        }

        private BatchStorageResult(boolean success, String batchTraceId, String errorMessage) {
            this.success = success;
            this.batchTraceId = batchTraceId;
            this.errorMessage = errorMessage;
        }

    }

    /**
     * 批次存储状态
     */
    enum BatchStorageStatus {
        /**
         * 未知状态
         */
        UNKNOWN,

        /**
         * 已接收但未存储
         */
        RECEIVED,

        /**
         * 存储中
         */
        STORING,

        /**
         * 已存储
         */
        STORED,

        /**
         * 存储失败
         */
        FAILED
    }

}
