package cn.cug.sxy.domain.reception.model.entity;

import cn.cug.sxy.domain.reception.model.valobj.*;
import cn.cug.sxy.types.model.Entity;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0
 * @Date 2025/7/7 11:37
 * @Description 日志批次值对象
 * @Author jerryhotton
 */

@Data
public class LogBatchEntity implements Entity<BatchId> {

    /**
     * 批次ID
     */
    private final BatchId id;
    /**
     * 应用ID
     */
    private final String appId;
    /**
     * 接收端点ID
     */
    private final String endpointId;
    /**
     * 原始日志列表
     */
    private List<RawLog> logs;
    /**
     * 批次状态
     */
    private BatchStatus status;
    /**
     * 错误信息
     */
    private String errorMessage;
    /**
     * 接收时间
     */
    private final Instant receivedTime;
    /**
     * 处理时间
     */
    private Instant processedTime;
    /**
     * 最后处理时间
     */
    private Instant lastProcessTime;
    /**
     * 重试次数
     */
    private int retryCount;
    /**
     * 批次跟踪ID（下游系统返回的ID）
     */
    private String batchTraceId;
    /**
     * 批次处理优先级
     */
    private int priority;
    /**
     * 是否已确认被下游系统接收
     */
    private Boolean confirmed;

    public LogBatchEntity(BatchId id, String appId, String endpointId,
                          List<RawLog> logs, BatchStatus status, Instant receivedTime) {
        this.id = id;
        this.appId = appId;
        this.endpointId = endpointId;
        this.logs = new ArrayList<>(logs);
        this.status = status;
        this.receivedTime = receivedTime;
        this.retryCount = 0;
        this.priority = 0;
        this.confirmed = false;
    }

    /**
     * 标记为处理中
     */
    public void markAsProcessing() {
        this.status = BatchStatus.PROCESSING;
        this.lastProcessTime = Instant.now();
    }

    /**
     * 标记为处理完成
     */
    public void markAsProcessed(String batchTraceId) {
        this.status = BatchStatus.PROCESSED;
        this.processedTime = Instant.now();
        this.lastProcessTime = this.processedTime;
        this.confirmed = true;
        this.batchTraceId = batchTraceId;
    }

    /**
     * 标记为处理失败
     */
    public void markAsFailed(String errorMessage) {
        this.status = BatchStatus.FAILED;
        this.errorMessage = errorMessage;
        this.lastProcessTime = Instant.now();
    }

    /**
     * 标记为已确认
     */
    public void markAsConfirmed() {
        this.confirmed = true;
    }

    /**
     * 增加重试次数
     */
    public void incrementRetryCount() {
        this.retryCount++;
    }


    /**
     * 获取日志条数
     */
    public int getLogCount() {
        return logs.size();
    }

    /**
     * 获取总负载大小（字节）
     */
    public long getPayloadSize() {
        return logs.stream()
                .mapToLong(log -> log.getContent().getBytes().length)
                .sum();
    }

    /**
     * 获取处理时间（毫秒）
     */
    public long getProcessingTimeMs() {
        if (processedTime == null || receivedTime == null) {
            return 0;
        }
        return processedTime.toEpochMilli() - receivedTime.toEpochMilli();
    }

    /**
     * 获取等待时间（毫秒）
     * 从接收到现在或处理完成的时间
     */
    public long getWaitTimeMs() {
        Instant endTime = processedTime != null ? processedTime : Instant.now();
        return endTime.toEpochMilli() - receivedTime.toEpochMilli();
    }

    /**
     * 是否可以重试
     *
     * @param maxRetryCount 最大重试次数
     * @return 是否可以重试
     */
    public boolean canRetry(int maxRetryCount) {
        return status == BatchStatus.FAILED && retryCount < maxRetryCount;
    }

    /**
     * 是否需要确认
     */
    public boolean needsConfirmation() {
        return status == BatchStatus.PROCESSED && !confirmed;
    }

}
