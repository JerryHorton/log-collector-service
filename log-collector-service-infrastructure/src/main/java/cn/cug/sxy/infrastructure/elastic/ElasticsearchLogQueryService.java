package cn.cug.sxy.infrastructure.elastic;

import cn.cug.sxy.domain.storage.model.entity.LogDocument;
import cn.cug.sxy.domain.storage.model.valobj.LogQuery;
import cn.cug.sxy.domain.storage.model.valobj.LogQueryResult;
import cn.cug.sxy.infrastructure.elastic.mapping.StructuredFieldManager;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @version 1.0
 * @Date 2025/7/15 08:50
 * @Description Elasticsearch日志查询服务实现
 * @Author jerryhotton
 */

@Slf4j
@Service
public class ElasticsearchLogQueryService implements IElasticsearchLogQueryService {

    private final ElasticsearchClient elasticsearchClient;
    private final StructuredFieldManager structuredFieldManager;

    @Value("${elasticsearch.index-prefix:logs}")
    private String indexPrefix;

    public ElasticsearchLogQueryService(
            ElasticsearchClient elasticsearchClient,
            StructuredFieldManager structuredFieldManager) {
        this.elasticsearchClient = elasticsearchClient;
        this.structuredFieldManager = structuredFieldManager;
    }

    @Override
    public LogQueryResult queryLogs(LogQuery query) {
        try {
            log.info("开始执行日志查询: query={}", query);

            // 构建索引名称模式，根据时间范围确定要查询的索引
            String indexPattern = buildIndexPattern(query.getStartTime(), query.getEndTime());
            log.debug("查询索引模式: {}", indexPattern);
            // 构建搜索请求
            SearchRequest.Builder searchBuilder = new SearchRequest.Builder()
                    .index(indexPattern)
                    .size(query.getPageSize())
                    .from((query.getPageNumber() - 1) * query.getPageSize())
                    .ignoreUnavailable(true);
            // 添加查询条件
            searchBuilder.query(q -> q
                    .bool(b -> {
                        // 添加时间范围过滤
                        if (query.getStartTime() != null && query.getEndTime() != null) {
                            b.filter(f -> f
                                    .range(r -> r
                                            .field("timestamp")
                                            .gte(JsonData.of(query.getStartTime().toEpochMilli()))
                                            .lte(JsonData.of(query.getEndTime().toEpochMilli()))
                                    )
                            );
                        }
                        // 添加应用ID过滤
                        if (query.getAppId() != null && !query.getAppId().isEmpty()) {
                            b.filter(f -> f
                                    .term(t -> t
                                            .field("appId.keyword")
                                            .value(query.getAppId())
                                    )
                            );
                        }
                        // 添加日志级别过滤
                        if (query.getLevel() != null && !query.getLevel().isEmpty()) {
                            b.filter(f -> f
                                    .term(t -> t
                                            .field("level.keyword")
                                            .value(query.getLevel())
                                    )
                            );
                        }
                        // 添加标签过滤
                        if (query.getTags() != null && !query.getTags().isEmpty()) {
                            log.info("处理标签查询, tags={}", query.getTags());
                            for (Map.Entry<String, String> tag : query.getTags().entrySet()) {
                                String key = tag.getKey();
                                String value = tag.getValue();
                                log.debug("添加标签过滤: key={}, value={}", key, value);
                                // 使用布尔查询组合多种匹配方式，提高查询成功率
                                b.filter(f -> f
                                        .bool(tagBool -> tagBool
                                                // 使用keyword类型字段 (精确匹配)
                                                .should(s -> s
                                                        .term(t -> t
                                                                .field("tags." + key + ".keyword")
                                                                .value(value)
                                                        )
                                                )
                                        )
                                );
                            }
                        }
                        // 添加关键词搜索
                        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
                            enhanceKeywordSearch(query, b);
                        }

                        return b;
                    })
            );
            // 添加排序
            searchBuilder.sort(s -> s
                    .field(f -> f
                            .field("timestamp")
                            .order(query.isAscending() ? co.elastic.clients.elasticsearch._types.SortOrder.Asc : co.elastic.clients.elasticsearch._types.SortOrder.Desc)
                    )
            );
            SearchRequest searchRequest = searchBuilder.build();
            // 记录最终构建的查询请求
            log.info("最终ES查询请求: {}", searchRequest.toString());
            // 执行搜索
            SearchResponse<LogDocument> response = elasticsearchClient.search(
                    searchRequest,
                    LogDocument.class
            );
            // 处理结果
            List<LogDocument> logs = new ArrayList<>();
            for (Hit<LogDocument> hit : response.hits().hits()) {
                if (hit.source() != null) {
                    logs.add(hit.source());
                }
            }
            // 构建返回结果
            if (response.hits().total() != null) {
                return LogQueryResult.builder()
                        .logs(logs)
                        .totalHits(response.hits().total().value())
                        .pageNumber(query.getPageNumber())
                        .pageSize(query.getPageSize())
                        .build();
            } else {
                // 没有命中结果，返回空结果集
                return LogQueryResult.builder()
                        .logs(logs)
                        .totalHits(0L)
                        .pageNumber(query.getPageNumber())
                        .pageSize(query.getPageSize())
                        .build();
            }
        } catch (Exception e) {
            log.error("查询日志异常: query={}, error={}", query, e.getMessage(), e);
            if (query.getTags() != null && !query.getTags().isEmpty()) {
                log.error("标签查询失败，标签内容: {}", query.getTags());
            }

            return LogQueryResult.builder()
                    .logs(List.of())
                    .totalHits(0L)
                    .pageNumber(query.getPageNumber())
                    .pageSize(query.getPageSize())
                    .build();
        }
    }

    @Override
    public LogDocument getLogById(String logId) {
        try {
            // 获取最近30天的索引模式
            String indexPattern = buildRecentIndexPattern(30);
            // 构建搜索请求
            var response = elasticsearchClient.get(g -> g
                            .index(indexPattern)
                            .id(logId),
                    LogDocument.class
            );

            return response.source();
        } catch (Exception e) {
            log.error("通过ID获取日志异常: logId={}, error={}", logId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根据时间范围构建索引模式
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 索引模式字符串
     */
    private String buildIndexPattern(Instant startTime, Instant endTime) {
        if (startTime == null || endTime == null) {
            // 如果没有时间范围，返回最近7天的索引
            return buildRecentIndexPattern(7);
        }
        // 计算日期范围内的所有索引名
        LocalDate start = startTime.atZone(ZoneId.of("UTC")).toLocalDate();
        LocalDate end = endTime.atZone(ZoneId.of("UTC")).toLocalDate();
        List<String> indices = new ArrayList<>();
        LocalDate current = start;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        while (!current.isAfter(end)) {
            indices.add(indexPrefix + "-" + current.format(formatter));
            current = current.plusDays(1);
        }
        // 返回逗号分隔的索引列表
        return String.join(",", indices);
    }

    /**
     * 构建最近N天的索引模式
     *
     * @param days 天数
     * @return 索引模式字符串
     */
    private String buildRecentIndexPattern(int days) {
        List<String> indices = new ArrayList<>();
        LocalDate current = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        for (int i = 0; i < days; i++) {
            LocalDate date = current.minusDays(i);
            indices.add(indexPrefix + "-" + date.format(formatter));
        }

        return String.join(",", indices);
    }

    /**
     * 增强关键词搜索功能，支持结构化数据字段查询
     *
     * @param query     查询对象
     * @param boolQuery 布尔查询构建器
     */
    private void enhanceKeywordSearch(LogQuery query, co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder boolQuery) {
        if (query.getKeyword() == null || query.getKeyword().isEmpty()) {
            return;
        }
        String keyword = query.getKeyword();
        log.info("处理关键词搜索: keyword={}", keyword);
        // 使用布尔查询组合多种搜索方式
        boolQuery.must(m -> m
                .bool(keywordBool -> {
                    // 1. 搜索原始内容 (最高优先级)
                    keywordBool.should(s -> s
                            .match(mm -> mm
                                    .field("content")
                                    .query(keyword)
                                    .boost(3.0f)  // 内容匹配权重最高
                            )
                    );
                    // 2. 动态获取结构化数据中的可搜索字段列表
                    List<String> structuredFields = structuredFieldManager.getSearchableFields("structuredData");
                    log.info("获取到 {} 个结构化数据可搜索字段", structuredFields.size());
                    // 如果有获取到字段，直接使用multi_match
                    if (!structuredFields.isEmpty()) {
                        keywordBool.should(s -> s
                                .multiMatch(mm -> mm
                                        .fields("structuredData.message")
                                        .query(keyword)
                                        .boost(2.0f)
                                )
                        );
                    } else {
                        // 如果没有获取到动态字段，回退到静态字段列表
                        String[] fallbackFields = {
                                "structuredData.level",
                                "structuredData.logger",
                                "structuredData.thread",
                                "structuredData.timestamp",
                                "structuredData.message",
                                "structuredData.exceptionMessage",
                                "structuredData.exceptionType",
                                "structuredData.stackTrace",
                                "structuredData.userId",
                                "structuredData.traceId",
                                // 嵌套异常和堆栈信息
                                "structuredData.rootCause.message",
                                "structuredData.rootCause.type",
                                "structuredData.causedByExceptions.message",
                                "structuredData.causedByExceptions.type",
                                "structuredData.stackFrames.file",
                                "structuredData.stackFrames.line",
                                "structuredData.stackFrames.method"
                        };
                        for (String field : fallbackFields) {
                            keywordBool.should(s -> s
                                    .match(mm -> mm
                                            .field(field)
                                            .query(keyword)
                                            .boost(2.0f)
                                    )
                            );
                        }
                    }
                    // 3. 搜索日志基础字段
                    keywordBool.should(s -> s
                            .multiMatch(mm -> mm
                                    .query(keyword)
                                    .fields(List.of(
                                                    "source",
                                                    "hostname",
                                                    "level",
                                                    "appId",
                                                    "sourceId",
                                                    "content"
                                            )
                                    )
                                    .boost(1.5f)
                            )
                    );

                    return keywordBool.minimumShouldMatch("1");
                })
        );
    }

}
