package cn.cug.sxy.domain.preprocess.service.strategy;

import cn.cug.sxy.domain.reception.model.valobj.LogFormat;
import cn.cug.sxy.domain.reception.model.valobj.ProcessedLog;

import java.util.List;

/**
 * @version 1.0
 * @Date 2025/7/9 13:55
 * @Description 预处理策略抽象基类
 * @Author jerryhotton
 */

public abstract class AbstractPreprocessStrategy implements IPreprocessStrategy {
    
    private final LogFormat supportedFormat;
    
    protected AbstractPreprocessStrategy(LogFormat supportedFormat) {
        this.supportedFormat = supportedFormat;
    }
    
    @Override
    public LogFormat getSupportedFormat() {
        return supportedFormat;
    }
    
    @Override
    public void processBatch(List<ProcessedLog> logs) {
        // 默认实现：逐个处理
        for (ProcessedLog log : logs) {
            process(log);
        }
    }
    
    @Override
    public boolean supportsBatchProcessing() {
        // 默认不支持批量处理
        return false;
    }
} 