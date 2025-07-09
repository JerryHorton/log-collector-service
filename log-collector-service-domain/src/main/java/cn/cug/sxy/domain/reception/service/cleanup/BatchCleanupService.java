package cn.cug.sxy.domain.reception.service.cleanup;

import cn.cug.sxy.domain.reception.adapter.repository.ILogBatchRepository;
import cn.cug.sxy.domain.reception.model.entity.LogBatch;
import cn.cug.sxy.domain.reception.model.valobj.BatchStatus;
import cn.cug.sxy.domain.reception.service.config.CleanupConfig;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 批次清理服务
 * 负责清理已处理完成或失败的批次
 */
@Slf4j
@Service
public class BatchCleanupService {
    
    private final ILogBatchRepository batchRepository;
    private final CleanupConfig cleanupConfig;
    
    // 清理调度器
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    public BatchCleanupService(ILogBatchRepository batchRepository, CleanupConfig cleanupConfig) {
        this.batchRepository = batchRepository;
        this.cleanupConfig = cleanupConfig;
    }
    
    @PostConstruct
    public void init() {
        // 启动定时清理任务
        scheduler.scheduleAtFixedRate(
                this::cleanupBatches,
                cleanupConfig.getInitialDelayMinutes(),
                cleanupConfig.getIntervalMinutes(),
                TimeUnit.MINUTES
        );
        
        log.info("批次清理服务已启动，初始延迟={}分钟，间隔={}分钟",
                cleanupConfig.getInitialDelayMinutes(),
                cleanupConfig.getIntervalMinutes());
    }
    
    /**
     * 清理批次
     */
    public void cleanupBatches() {
        try {
            log.info("开始清理批次...");
            
            // 清理已处理完成的批次
            cleanupProcessedBatches();
            
            // 清理处理失败的批次
            cleanupFailedBatches();
            
            // 清理超时的批次
            cleanupTimeoutBatches();
            
            log.info("批次清理完成");
        } catch (Exception e) {
            log.error("批次清理异常", e);
        }
    }
    
    /**
     * 清理已处理完成的批次
     */
    private void cleanupProcessedBatches() {
        // 计算清理阈值时间
        Instant threshold = Instant.now().minus(cleanupConfig.getProcessedRetentionHours(), ChronoUnit.HOURS);
        
        // 查询需要清理的批次
        List<LogBatch> batches = batchRepository.findByStatusAndProcessedTimeBefore(BatchStatus.PROCESSED, threshold);
        
        if (!batches.isEmpty()) {
            log.info("清理已处理完成的批次，数量={}", batches.size());
            
            // 删除批次
            for (LogBatch batch : batches) {
                batchRepository.delete(batch);
            }
        }
    }
    
    /**
     * 清理处理失败的批次
     */
    private void cleanupFailedBatches() {
        // 计算清理阈值时间
        Instant threshold = Instant.now().minus(cleanupConfig.getFailedRetentionHours(), ChronoUnit.HOURS);
        
        // 查询需要清理的批次
        List<LogBatch> batches = batchRepository.findByStatusAndLastProcessTimeBefore(BatchStatus.FAILED, threshold);
        
        if (!batches.isEmpty()) {
            log.info("清理处理失败的批次，数量={}", batches.size());
            
            // 删除批次
            for (LogBatch batch : batches) {
                batchRepository.delete(batch);
            }
        }
    }
    
    /**
     * 清理超时的批次
     */
    private void cleanupTimeoutBatches() {
        // 计算清理阈值时间
        Instant threshold = Instant.now().minus(cleanupConfig.getTimeoutMinutes(), ChronoUnit.MINUTES);
        
        // 查询需要清理的批次
        List<LogBatch> batches = batchRepository.findByStatusAndLastProcessTimeBefore(BatchStatus.PROCESSING, threshold);
        
        if (!batches.isEmpty()) {
            log.info("清理超时的批次，数量={}", batches.size());
            
            // 将超时批次标记为失败
            for (LogBatch batch : batches) {
                batch.markAsFailed("处理超时");
                batchRepository.save(batch);
            }
        }
    }
    
    /**
     * 手动触发清理
     */
    public void triggerCleanup() {
        log.info("手动触发批次清理");
        cleanupBatches();
    }

} 