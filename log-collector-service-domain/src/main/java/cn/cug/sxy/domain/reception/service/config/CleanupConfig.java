package cn.cug.sxy.domain.reception.service.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 批次清理配置
 */
@Getter
@Component
public class CleanupConfig {
    
    /**
     * 已处理批次保留时间（小时）
     */
    @Value("${log-center.reception.cleanup.processed-retention-hours:24}")
    private int processedRetentionHours;
    
    /**
     * 失败批次保留时间（小时）
     */
    @Value("${log-center.reception.cleanup.failed-retention-hours:72}")
    private int failedRetentionHours;
    
    /**
     * 批次处理超时时间（分钟）
     */
    @Value("${log-center.reception.cleanup.timeout-minutes:30}")
    private int timeoutMinutes;
    
    /**
     * 清理任务初始延迟（分钟）
     */
    @Value("${log-center.reception.cleanup.initial-delay-minutes:5}")
    private int initialDelayMinutes;
    
    /**
     * 清理任务间隔（分钟）
     */
    @Value("${log-center.reception.cleanup.interval-minutes:60}")
    private int intervalMinutes;

} 