package cn.cug.sxy.domain.reception.service;

import cn.cug.sxy.domain.reception.model.valobj.BatchId;

/**
 * @version 1.0
 * @Date 2025/7/9 16:47
 * @Description 批次处理服务接口
 * @Author jerryhotton
 */

public interface IBatchProcessingService {

    /**
     * 异步处理批次
     * <p>
     * 该方法负责将日志批次传递给下游处理系统，并确保：
     * 1. 幂等性处理 - 同一批次不会被重复处理
     * 2. 错误重试 - 处理失败时进行有限次数的重试
     * 3. 状态追踪 - 记录批次处理的状态和结果
     * 4. 领域边界 - 将日志批次传递给存储/分析领域进行后续处理
     *
     * @param batchId 批次ID
     */
    void processBatchAsync(BatchId batchId);

}
