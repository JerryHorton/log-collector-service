package cn.cug.sxy.domain.storage.model.entity;

import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * @version 1.0
 * @Date 2025/7/11 15:13
 * @Description LogDocument
 * @Author jerryhotton
 */

@Data
public class LogDocument {

    /**
     * 文档唯一标识符，使用UUID生成
     */
    private String id;
    /**
     * 批次ID，用于将同批次的日志关联起来
     * 对应LogBatch中的批次ID
     */
    private String batchId;
    /**
     * 应用ID，标识日志来源的应用
     * 用于跨应用查询和权限控制
     */
    private String appId;
    /**
     * 端点ID，标识接收日志的端点
     * 用于分析不同接入点的日志数据
     */
    private String endpointId;
    /**
     * 日志内容，存储原始日志文本
     * 可能是文本、JSON字符串等
     */
    private String content;
    /**
     * 日志格式，如JSON、TEXT、XML等
     * 用于解析和展示日志内容
     */
    private String format;
    /**
     * 来源ID，标识日志的来源系统或组件
     * 更细粒度地区分日志来源
     */
    private String sourceId;
    /**
     * 时间戳，记录日志生成的时间(毫秒)
     * 用于时间序列分析和查询过滤
     */
    private Long timestamp;
    /**
     * 日志标签，存储key-value格式的标签信息
     * 用于日志分类和过滤
     */
    private Map<String, String> tags;
    /**
     * 日志来源，如服务名称
     * 例如：user-service, order-service
     */
    private String source;
    /**
     * 主机名，记录生成日志的主机信息
     * 例如：user-service-pod-1, host-name-01
     */
    private String hostname;
    /**
     * 日志级别，如INFO、WARN、ERROR等
     * 用于区分日志的重要程度
     */
    private String level;
    /**
     * 结构化数据，存储解析后的JSON字段
     * 用于高效检索和分析特定字段
     */
    private Map<String, Object> structuredData;
    /**
     * 用于存储所有结构化数据的值
     */
    private String structuredDataText;
    /**
     * 索引时间，记录日志被索引到ES的时间
     * 用于区分日志生成时间和接收时间
     */
    private Instant indexTime;

}
