package cn.cug.sxy.domain.reception.alc;

import cn.cug.sxy.domain.reception.model.entity.LogBatch;
import cn.cug.sxy.domain.reception.model.valobj.AppId;
import cn.cug.sxy.domain.reception.model.valobj.EndpointId;
import cn.cug.sxy.domain.reception.model.valobj.ProcessedLog;
import cn.cug.sxy.domain.reception.model.valobj.RawLog;

import java.util.List;

/**
 * @version 1.0
 * @Date 2025/7/9 11:56
 * @Description 预处理领域防腐层接口
 * @Author jerryhotton
 */

public interface IPreprocessGateway {

    /**
     * 预处理单条日志
     *
     * @param rawLog 原始日志
     * @param appId 应用ID
     * @param endpointId 端点ID
     * @return 处理后的日志
     */
    ProcessedLog preprocess(RawLog rawLog, AppId appId, EndpointId endpointId);

    /**
     * 批量预处理日志
     *
     * @param rawLogs 原始日志列表
     * @param appId 应用ID
     * @param endpointId 端点ID
     * @return 处理后的日志列表
     */
    List<ProcessedLog> preprocessBatch(List<RawLog> rawLogs, AppId appId, EndpointId endpointId);

    /**
     * 从批次中预处理日志
     *
     * @param batch 日志批次
     * @return 处理后的日志列表
     */
    List<ProcessedLog> preprocessFromBatch(LogBatch batch);

}
