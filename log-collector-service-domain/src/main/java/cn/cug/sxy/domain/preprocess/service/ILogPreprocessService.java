package cn.cug.sxy.domain.preprocess.service;

import cn.cug.sxy.domain.reception.model.entity.LogBatchEntity;
import cn.cug.sxy.domain.reception.model.valobj.ProcessedLog;
import cn.cug.sxy.domain.reception.model.valobj.RawLog;

import java.util.List;

/**
 * @version 1.0
 * @Date 2025/7/9 13:54
 * @Description 日志预处理服务接口（负责将原始日志转换为处理后的日志）
 * @Author jerryhotton
 */

public interface ILogPreprocessService {

    /**
     * 预处理单条日志
     *
     * @param rawLog 原始日志
     * @param appId 应用ID
     * @param endpointId 端点ID
     * @return 处理后的日志
     */
    ProcessedLog preprocess(RawLog rawLog, String appId, String endpointId);

    /**
     * 批量预处理日志
     *
     * @param rawLogs 原始日志列表
     * @param appId 应用ID
     * @param endpointId 端点ID
     * @return 处理后的日志列表
     */
    List<ProcessedLog> preprocessBatch(List<RawLog> rawLogs, String appId, String endpointId);

    /**
     * 从批次中预处理日志
     *
     * @param batch 日志批次
     * @return 处理后的日志列表
     */
    List<ProcessedLog> preprocessFromBatch(LogBatchEntity batch);

}
