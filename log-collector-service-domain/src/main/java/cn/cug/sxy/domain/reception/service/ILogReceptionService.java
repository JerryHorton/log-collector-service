package cn.cug.sxy.domain.reception.service;

import cn.cug.sxy.domain.reception.model.valobj.*;

import java.util.List;

/**
 * @version 1.0
 * @Date 2025/7/7 10:32
 * @Description 日志接收服务接口（负责接收来自不同来源的日志数据）
 * @Author jerryhotton
 */

public interface ILogReceptionService {

    /**
     * 接收单条原始日志
     *
     * @param rawLog 原始日志
     * @param appId 应用ID
     * @param endpointId 端点ID
     * @return 日志接收结果
     */
    ReceptionResult receiveLog(RawLog rawLog, AppId appId, EndpointId endpointId);

    /**
     * 批量接收原始日志
     *
     * @param rawLogs 原始日志列表
     * @param appId 应用ID
     * @param endpointId 端点ID
     * @return 日志接收结果
     */
    ReceptionResult receiveBatch(List<RawLog> rawLogs, AppId appId, EndpointId endpointId);

    /**
     * 处理接收到的日志批次
     *
     * @param batchId 批次ID
     * @return 处理结果
     */
    ProcessResult processBatch(BatchId batchId);

    /**
     * 查询批次处理状态
     *
     * @param batchId 批次ID
     * @return 批次状态
     */
    BatchStatus queryBatchStatus(BatchId batchId);

}
