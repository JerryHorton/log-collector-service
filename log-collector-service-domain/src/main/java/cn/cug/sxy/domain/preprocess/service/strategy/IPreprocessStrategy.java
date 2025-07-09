package cn.cug.sxy.domain.preprocess.service.strategy;

import cn.cug.sxy.domain.reception.model.valobj.LogFormat;
import cn.cug.sxy.domain.reception.model.valobj.ProcessedLog;

import java.util.List;

/**
 * @version 1.0
 * @Date 2025/7/9 13:55
 * @Description 日志预处理策略接口
 * @Author jerryhotton
 */

public interface IPreprocessStrategy {
    
    /**
     * 获取支持的日志格式
     * 
     * @return 日志格式
     */
    LogFormat getSupportedFormat();
    
    /**
     * 处理单条日志
     * 
     * @param log 待处理的日志
     */
    void process(ProcessedLog log);
    
    /**
     * 批量处理日志
     * 
     * @param logs 待处理的日志列表
     */
    void processBatch(List<ProcessedLog> logs);
    
    /**
     * 是否支持批量处理
     * 
     * @return 是否支持批量处理
     */
    boolean supportsBatchProcessing();
}