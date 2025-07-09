package cn.cug.sxy.infrastructure.dao.po;

import lombok.Data;

import java.util.Date;

/**
 * @version 1.0
 * @Date 2025/7/7 10:06
 * @Description 接收日志缓冲持久层对象
 * @Author jerryhotton
 */

@Data
public class LogReceptionBuffer {

    /**
     * 主键ID
     */
    private Long id;
    /**
     * 批次ID
     */
    private String batchId;
    /**
     * 应用ID
     */
    private String appId;
    /**
     * 接收端点ID
     */
    private String endpointId;
    /**
     * 日志条数
     */
    private Integer logCount;
    /**
     * 负载大小(字节)
     */
    private Integer payloadSize;
    /**
     * 状态(PENDING/PROCESSED/FAILED)
     */
    private String status;
    /**
     * 错误信息
     */
    private String errorMessage;
    /**
     * 接收时间
     */
    private Date receivedTime;
    /**
     * 处理时间
     */
    private Date processedTime;
    /**
     * 创建时间
     */
    private Date createdTime;

}
