package cn.cug.sxy.api.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * @version 1.0
 * @Date 2025/7/10 16:37
 * @Description 批量日志接收请求DTO
 * @Author jerryhotton
 */

@Data
public class BatchLogReceiveRequestDTO {

    /**
     * 应用ID
     */
    private String appId;
    /**
     * 接入端点ID
     */
    private String endpointId;
    /**
     * 日志格式，所有日志共用一种格式
     */
    private String format;
    /**
     * 公共来源ID
     * 用于标识日志的来源系统或组件
     */
    private String commonSourceId;
    /**
     * 日志列表
     */
    private List<LogEntry> logs;
    /**
     * 公共标签
     */
    private String commonTags;
    /**
     * 公共来源
     */
    private String commonSource;
    /**
     * 公共主机名
     */
    private String commonHostname;

    /**
     * 单条日志条目
     */
    @Data
    public static class LogEntry {

        /**
         * 日志内容
         */
        private String content;
        /**
         * 来源ID
         * 用于标识日志的来源系统或组件
         */
        private String sourceId;
        /**
         * 日志级别
         */
        private String level;
        /**
         * 日志时间戳
         */
        private Long timestamp;
        /**
         * 日志标签
         */
        private String tags;

    }

}
