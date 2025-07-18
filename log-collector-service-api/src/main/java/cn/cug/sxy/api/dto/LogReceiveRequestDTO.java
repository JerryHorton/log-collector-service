package cn.cug.sxy.api.dto;

import lombok.Data;

import java.util.Map;

/**
 * @version 1.0
 * @Date 2025/7/10 16:39
 * @Description 日志接收请求DTO
 * @Author jerryhotton
 */

@Data
public class LogReceiveRequestDTO {

    /**
     * 应用ID
     */
    private String appId;
    /**
     * 接入端点ID
     */
    private String endpointId;
    /**
     * 来源ID
     * 用于标识日志的来源系统或组件
     */
    private String sourceId;
    /**
     * 日志来源
     * 例如: user-service, order-service
     */
    private String source;
    /**
     * 日志内容
     */
    private String content;
    /**
     * 日志格式
     * 有效值: JSON, TEXT, XML, CSV, BINARY
     */
    private String format;
    /**
     * 时间戳
     * 日志产生的时间戳(毫秒)
     */
    private Long timestamp;
    /**
     * 日志标签
     * 格式: key1=value1,key2=value2
     */
    private String tags;
    /**
     * 日志级别
     * 例如: INFO, WARN, ERROR
     */
    private String level;
    /**
     * 日志主机名
     * 例如: user-service-pod-1, host-name-01
     */
    private String hostname;
    /**
     * 结构化数据
     */
    private Map<String, Object> structuredData;

}
