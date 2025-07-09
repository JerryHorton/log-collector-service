package cn.cug.sxy.infrastructure.dao.po;

import lombok.Data;

import java.util.Date;

/**
 * @version 1.0
 * @Date 2025/7/7 10:03
 * @Description 日志接收端点持久层对象
 * @Author jerryhotton
 */

@Data
public class LogReceiverEndpoint {

    /**
     * 主键ID
     */
    private Long id;
    /**
     * 端点唯一标识
     */
    private String endpointId;
    /**
     * 端点名称
     */
    private String name;
    /**
     * 端点类型(HTTP/SDK/AGENT)
     */
    private String type;
    /**
     * 协议(HTTP/HTTPS/TCP/UDP)
     */
    private String protocol;
    /**
     * 接收路径(HTTP路径或Socket地址)
     */
    private String path;
    /**
     * 端口号
     */
    private Integer port;
    /**
     * 接收格式(JSON/TEXT/BINARY)
     */
    private String format;
    /**
     * 最大负载大小(字节)
     */
    private Integer maxPayloadSize;
    /**
     * 是否启用压缩
     */
    private Boolean compressionEnabled;
    /**
     * 压缩算法(GZIP/DEFLATE)
     */
    private String compressionAlgorithm;
    /**
     * 状态(ACTIVE/INACTIVE)
     */
    private String status;
    /**
     * 允许的应用ID列表(逗号分隔)
     */
    private String allowedAppIds;
    /**
     * 创建时间
     */
    private Date createdTime;
    /**
     * 更新时间
     */
    private Date updatedTime;

}
