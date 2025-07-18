package cn.cug.sxy.infrastructure.dao.po;

import lombok.Data;

import java.util.Date;

/**
 * @version 1.0
 * @Date 2025/7/11 15:55
 * @Description 日志批次持久层对象
 * @Author jerryhotton
 */

@Data
public class LogBatch {

    /**
     * 主键ID
     */
    private Long id;
    /**
     * 批次ID，对应BatchId值对象
     */
    private String batchId;
    /**
     * 应用ID，关联应用表
     */
    private String appId;
    /**
     * 接收端点ID，关联接收端点表
     */
    private String endpointId;
    /**
     * 批次状态: PENDING/PROCESSING/PROCESSED/FAILED/RETRY
     */
    private String status;
    /**
     * 错误信息，记录处理失败原因
     */
    private String errorMessage;
    /**
     * 批次跟踪ID（下游系统返回的ID，如ES返回的ID）
     */
    private String batchTraceId;
    /**
     * 是否已确认被下游系统接收，0否1是
     */
    private boolean confirmed;
    /**
     * 日志数量
     */
    private int logCount;
    /**
     * 总负载大小（字节）
     */
    private long payloadSize;
    /**
     * 重试次数
     */
    private int retryCount;
    /**
     * 处理优先级，数值越高优先级越高
     */
    private int priority;
    /**
     * 接收时间
     */
    private Date receivedTime;
    /**
     * 处理完成时间
     */
    private Date processedTime;
    /**
     * 最后处理时间（含重试）
     */
    private Date lastProcessTime;
    /**
     * 主要日志格式: JSON/TEXT/XML/CSV/BINARY
     */
    private String format;
    /**
     * 日志主要来源，例如 service 名称
     */
    private String source;
    /**
     * 提交批次的客户端 IP
     */
    private String sourceIp;
    /**
     * ES索引名称
     */
    private String storageIndex;
    /**
     * 存储类型：elasticsearch/s3/hdfs等
     */
    private String storageType;
    /**
     * 存储路径或位置
     */
    private String storagePath;
    /**
     * 创建时间
     */
    private Date createdTime;
    /**
     * 更新时间
     */
    private Date updatedTime;

}
