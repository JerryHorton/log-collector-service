package cn.cug.sxy.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * @version 1.0
 * @Date 2025/7/15 09:26
 * @Description 日志查询响应
 * @Author jerryhotton
 */

@Data
@Builder
public class LogQueryResponseDTO {

    /**
     * 日志列表
     */
    private List<LogDocument> logs;
    /**
     * 总命中数
     */
    private long totalHits;
    /**
     * 当前页码
     */
    private int pageNumber;
    /**
     * 每页大小
     */
    private int pageSize;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LogDocument {

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
         * 索引时间，记录日志被索引到ES的时间
         * 用于区分日志生成时间和接收时间
         */
        private Instant indexTime;

    }


}
