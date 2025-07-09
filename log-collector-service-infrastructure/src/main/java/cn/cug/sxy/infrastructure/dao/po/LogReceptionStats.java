package cn.cug.sxy.infrastructure.dao.po;

import lombok.Data;

import java.util.Date;

/**
 * @version 1.0
 * @Date 2025/7/7 10:07
 * @Description 接收统计持久层对象
 * @Author jerryhotton
 */

@Data
public class LogReceptionStats {

    /**
     * 主键ID
     */
    private Long id;
    /**
     * 应用ID
     */
    private String appId;
    /**
     * 接收端点ID
     */
    private String endpointId;
    /**
     * 统计日期
     */
    private java.sql.Date statDate;
    /**
     * 统计小时(0-23)
     */
    private Integer statHour;
    /**
     * 接收日志数
     */
    private Long receivedCount;
    /**
     * 接收字节数
     */
    private Long receivedBytes;
    /**
     * 成功处理数
     */
    private Long successCount;
    /**
     * 失败处理数
     */
    private Long failedCount;
    /**
     * 平均处理时间(毫秒)
     */
    private Integer avgProcessTime;
    /**
     * 最大处理时间(毫秒)
     */
    private Integer maxProcessTime;
    /**
     * 创建时间
     */
    private Date createdTime;
    /**
     * 更新时间
     */
    private Date updatedTime;

}
