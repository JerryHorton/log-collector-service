package cn.cug.sxy.domain.reception.service.config;

import cn.cug.sxy.domain.reception.model.valobj.EndpointId;
import cn.cug.sxy.domain.reception.model.valobj.EndpointStatus;
import cn.cug.sxy.domain.reception.model.valobj.LogFormat;
import lombok.Getter;

/**
 * 端点配置值对象
 */
@Getter
public class EndpointConfig {
    
    // 端点ID
    private final EndpointId endpointId;
    
    // 端点名称
    private final String name;
    
    // 日志格式
    private final LogFormat format;
    
    // 端点状态
    private final EndpointStatus status;
    
    // 最大负载大小（字节）
    private final int maxPayloadSize;
    
    // 最大批次大小（字节）
    private final int maxBatchSize;
    
    // 最大批次数量
    private final int maxBatchCount;
    
    public EndpointConfig(EndpointId endpointId, String name, LogFormat format,
                         EndpointStatus status, int maxPayloadSize, int maxBatchSize,
                         int maxBatchCount) {
        this.endpointId = endpointId;
        this.name = name;
        this.format = format;
        this.status = status;
        this.maxPayloadSize = maxPayloadSize;
        this.maxBatchSize = maxBatchSize;
        this.maxBatchCount = maxBatchCount;
    }
    
    /**
     * 检查日志大小是否超限
     * 
     * @param logSize 日志大小（字节）
     * @return 是否超限
     */
    public boolean isLogSizeExceeded(int logSize) {
        return logSize > maxPayloadSize;
    }
    
    /**
     * 检查批次大小是否超限
     * 
     * @param batchSize 批次大小（字节）
     * @return 是否超限
     */
    public boolean isBatchSizeExceeded(int batchSize) {
        return batchSize > maxBatchSize;
    }
    
    /**
     * 检查批次数量是否超限
     * 
     * @param batchCount 批次数量
     * @return 是否超限
     */
    public boolean isBatchCountExceeded(int batchCount) {
        return maxBatchCount > 0 && batchCount > maxBatchCount;
    }

} 