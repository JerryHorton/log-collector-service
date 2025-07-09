package cn.cug.sxy.infrastructure.dao.po;

import lombok.Data;

import java.util.Date;

/**
 * @version 1.0
 * @Date 2025/7/7 10:09
 * @Description 接收认证日志持久层对象
 * @Author jerryhotton
 */

@Data
public class LogReceptionAuthLog {

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
     * 客户端IP
     */
    private String clientIp;
    /**
     * 认证方法(ACCESSKEY/TOKEN/CERT)
     */
    private String authMethod;
    /**
     * 认证结果(SUCCESS/FAILED)
     */
    private String authResult;
    /**
     * 错误代码
     */
    private String errorCode;
    /**
     * 错误信息
     */
    private String errorMessage;
    /**
     * 请求时间
     */
    private Date requestTime;
    /**
     * 创建时间
     */
    private Date createdTime;

}
