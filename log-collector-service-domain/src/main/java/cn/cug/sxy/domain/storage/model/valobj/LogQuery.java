package cn.cug.sxy.domain.storage.model.valobj;

import cn.cug.sxy.types.model.ValueObject;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * @version 1.0
 * @Date 2025/7/15 08:46
 * @Description 日志查询条件值对象
 * @Author jerryhotton
 */

@Getter
@Builder
public class LogQuery implements ValueObject {

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
