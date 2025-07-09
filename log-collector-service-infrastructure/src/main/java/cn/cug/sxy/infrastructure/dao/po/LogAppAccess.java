package cn.cug.sxy.infrastructure.dao.po;

import lombok.Data;

import java.util.Date;

/**
 * @version 1.0
 * @Date 2025/7/7 10:05
 * @Description 接入应用持久层对象
 * @Author jerryhotton
 */

@Data
public class LogAppAccess {

    /**
     * 主键ID
     */
    private Long id;
    /**
     * 应用ID
     */
    private String appId;
    /**
     * 应用名称
     */
    private String appName;
    /**
     * 访问密钥
     */
    private String accessKey;
    /**
     * 密钥(加密存储)
     */
    private String secretKey;
    /**
     * 允许的端点ID列表(逗号分隔)
     */
    private String allowedEndpoints;
    /**
     * IP白名单(逗号分隔)
     */
    private String ipWhitelist;
    /**
     * 速率限制(次/分钟)
     */
    private Integer rateLimit;
    /**
     * 状态(ACTIVE/INACTIVE/BLOCKED)
     */
    private String status;
    /**
     * 过期时间
     */
    private Date expiryTime;
    /**
     * 创建时间
     */
    private Date createdTime;
    /**
     * 更新时间
     */
    private Date updatedTime;

}
