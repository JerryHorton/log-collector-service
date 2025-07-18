package cn.cug.sxy.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * @version 1.0
 * @Date 2025/7/15 09:29
 * @Description 日志查询请求
 * @Author jerryhotton
 */

@Data
@Builder
public class LogQueryRequestDTO {

    /**
     * 开始时间
     */
    private Instant startTime;
    /**
     * 结束时间
     */
    private Instant endTime;
    /**
     * 应用ID
     */
    private String appId;
    /**
     * 日志级别
     */
    private String level;
    /**
     * 标签过滤条件
     */
    private Map<String, String> tags;
    /**
     * 关键词搜索
     */
    private String keyword;
    /**
     * 页码，从1开始
     */
    @Builder.Default
    private int pageNumber = 1;
    /**
     * 每页大小
     */
    @Builder.Default
    private int pageSize = 20;
    /**
     * 是否按时间升序排序，默认降序（最新的先显示）
     */
    @Builder.Default
    private boolean ascending = false;

}
