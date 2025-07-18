package cn.cug.sxy.infrastructure.elastic.mapping;

import cn.cug.sxy.infrastructure.redis.IRedisService;
import cn.cug.sxy.types.common.Constants;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.GetMappingResponse;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @version 1.0
 * @Date 2025/7/17 17:15
 * @Description 结构化字段管理器（负责自动同步ES索引mapping，维护可搜索字段列表）
 * @Author jerryhotton
 */

@Slf4j
@Component
public class StructuredFieldManager {

    private final ElasticsearchClient elasticsearchClient;
    private final IRedisService redisService;

    @Value("${elasticsearch.index-prefix:logs}")
    private String indexPrefix;

    @Value("${elasticsearch.field-refresh-interval-ms:300000}")
    private long refreshInterval;

    @Value("${elasticsearch.dynamic-fields.enabled:true}")
    private boolean dynamicFieldsEnabled;

    @Value("${elasticsearch.cache.local-expiration-ms:60000}")
    private long localCacheExpirationMs = 60000;

    @Value("${elasticsearch.cache.redis-expiration-ms:600000}")
    private long redisCacheExpirationMs = 600000;

    // 本地Guava缓存
    private final Cache<String, List<String>> localCache;

    // 记录最后一次刷新时间
    private volatile long lastRefreshTime = 0;

    public StructuredFieldManager(
            ElasticsearchClient elasticsearchClient,
            IRedisService redisService) {
        this.elasticsearchClient = elasticsearchClient;
        this.redisService = redisService;
        // 初始化Guava缓存
        this.localCache = CacheBuilder.newBuilder()
                .expireAfterWrite(localCacheExpirationMs, TimeUnit.MILLISECONDS)
                .maximumSize(100)
                .recordStats()
                .build();
    }

    /**
     * 初始化时加载字段
     */
    @PostConstruct
    public void init() {
        refreshFields();
    }

    /**
     * 定期刷新字段信息
     */
    @Scheduled(fixedRateString = "${elasticsearch.field-refresh-interval-ms:300000}")
    public void scheduledRefresh() {
        refreshFields();
    }

    /**
     * 刷新字段信息
     * 从最新的索引中获取mapping，并提取可搜索字段
     */
    public synchronized void refreshFields() {
        try {
            log.info("开始刷新ES索引字段信息");
            // 获取最新的索引
            String latestIndex = getLatestIndexName();
            if (latestIndex == null) {
                log.warn("未找到有效的索引，跳过字段刷新");
                return;
            }
            log.info("从索引 {} 获取mapping", latestIndex);
            // 获取索引mapping
            GetMappingResponse mappingResponse = elasticsearchClient.indices()
                    .getMapping(builder -> builder.index(latestIndex));
            // 获取TypeMapping对象
            TypeMapping typeMapping = Objects.requireNonNull(mappingResponse.get(latestIndex)).mappings();
            if (typeMapping == null || typeMapping.properties() == null || typeMapping.properties().isEmpty()) {
                log.warn("索引 {} 没有有效的mapping或properties为空，跳过字段刷新", latestIndex);
                return;
            }
            // 提取结构化数据字段
            Set<String> structuredFields = new HashSet<>();
            extractSearchableFields(typeMapping.properties(), "structuredData", "", structuredFields);
            // 更新缓存
            if (!structuredFields.isEmpty()) {
                // 更新redis缓存
                List<String> fieldsList = new ArrayList<>(structuredFields);
                String redisKey = getRedisKey("structuredData");
                redisService.setValue(redisKey, fieldsList, redisCacheExpirationMs, TimeUnit.MILLISECONDS);
                // 更新本地缓存
                localCache.put("structuredData", fieldsList);
                lastRefreshTime = System.currentTimeMillis();
                log.info("ES索引字段刷新完成，共提取 {} 个结构化数据字段", structuredFields.size());
            } else {
                log.warn("未从索引 {} 中提取到任何结构化数据字段", latestIndex);
                // 如果没有提取到字段，使用默认字段
                List<String> defaultFields = getDefaultFields("structuredData");
                String redisKey = getRedisKey("structuredData");
                redisService.setValue(redisKey, defaultFields, redisCacheExpirationMs, TimeUnit.MILLISECONDS);
                // 更新本地缓存
                localCache.put("structuredData", defaultFields);
                log.info("已加载默认字段列表作为备选");
            }
        } catch (Exception e) {
            log.error("刷新ES索引字段信息失败: {}", e.getMessage(), e);
            // 确保至少有默认字段可用
            List<String> defaultFields = getDefaultFields("structuredData");
            // 尝试更新Redis缓存
            try {
                String redisKey = getRedisKey("structuredData");
                redisService.setValue(redisKey, defaultFields, redisCacheExpirationMs, TimeUnit.MILLISECONDS);
            } catch (Exception ex) {
                log.error("更新Redis缓存失败: {}", ex.getMessage(), ex);
            }
            // 更新本地缓存
            localCache.put("structuredData", defaultFields);
        }
    }

    /**
     * 递归提取可搜索字段
     *
     * @param properties   属性映射
     * @param targetObject 目标对象路径（如"structuredData"）
     * @param currentPath  当前处理的路径
     * @param result       结果集合，用于存储找到的字段
     */
    private void extractSearchableFields(Map<String, Property> properties,
                                         String targetObject,
                                         String currentPath,
                                         Set<String> result) {
        if (properties == null || properties.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Property> entry : properties.entrySet()) {
            String fieldName = entry.getKey();
            Property property = entry.getValue();
            // 构建完整路径
            String fullPath = currentPath.isEmpty() ? fieldName : currentPath + "." + fieldName;
            // 检查是否在目标对象路径下
            boolean isTargetPath = targetObject.isEmpty() ||
                    fullPath.equals(targetObject) ||
                    fullPath.startsWith(targetObject + ".");
            if (isTargetPath) {
                // 检查是否为可搜索字段类型（text或keyword）
                if (property.isText() || property.isKeyword()) {
                    result.add(fullPath);
                    log.debug("添加可搜索字段: {}", fullPath);
                }
            }
            // 递归处理嵌套对象和嵌套数组
            Map<String, Property> nestedProperties = null;
            if (property.isObject() && property.object() != null) {
                nestedProperties = property.object().properties();
            } else if (property.isNested() && property.nested() != null) {
                nestedProperties = property.nested().properties();
            }
            // 如果是嵌套属性，继续递归
            if (nestedProperties != null && !nestedProperties.isEmpty()) {
                extractSearchableFields(nestedProperties, targetObject, fullPath, result);
            }
        }
    }

    /**
     * 获取最新的索引名称
     */
    private String getLatestIndexName() {
        try {
            // 使用通配符获取所有相关索引
            String pattern = indexPrefix + "-*";
            var response = elasticsearchClient.indices().get(builder ->
                    builder.index(pattern));
            // 找出最新的索引
            return response.result().keySet().stream()
                    .sorted(Comparator.reverseOrder())
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            log.error("获取最新索引失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取指定对象的所有可搜索字段
     */
    public List<String> getSearchableFields(String objectName) {
        // 如果未启用动态字段返回默认字段
        if (!dynamicFieldsEnabled) {
            return getDefaultFields(objectName);
        }
        // 尝试从本地缓存获取
        List<String> fields = localCache.getIfPresent(objectName);
        if (null != fields && !fields.isEmpty()) {
            log.debug("从本地缓存获取到 {} 个字段", fields.size());
            return fields;
        }
        // 尝试从redis获取
        try {
            String redisKey = getRedisKey(objectName);
            fields = redisService.getValue(redisKey);
            if (null != fields && !fields.isEmpty()) {
                // 更新本地缓存
                localCache.put(objectName, fields);
                log.debug("从redis缓存获取到 {} 个字段", fields.size());
                return fields;
            }
        } catch (Exception e) {
            log.warn("从Redis获取字段信息失败: {}", e.getMessage());
        }
        // 缓存都未命中尝试刷新字段
        long now = System.currentTimeMillis();
        if (now - lastRefreshTime > refreshInterval) {
            refreshFields();
            // 再次尝试从本地缓存获取
            fields = localCache.getIfPresent(objectName);
            if (fields != null && !fields.isEmpty()) {
                return fields;
            }
        }

        return getDefaultFields(objectName);
    }

    /**
     * 获取最后一次刷新时间
     *
     * @return 最后刷新时间的毫秒时间戳
     */
    public long getLastRefreshTime() {
        return lastRefreshTime;
    }

    /**
     * 默认的可搜索字段列表
     */
    private List<String> getDefaultFields(String objectName) {
        if ("structuredData".equals(objectName)) {
            return Arrays.asList(
                    "structuredData.message",
                    "structuredData.exceptionMessage",
                    "structuredData.exceptionType",
                    "structuredData.stackTrace",
                    "structuredData.userId",
                    "structuredData.traceId"
            );
        }
        return Collections.emptyList();
    }

    /**
     * 构建Redis缓存键
     */
    private String getRedisKey(String objectName) {
        return Constants.RedisKey.ELASTICSEARCH_STRUCTURED_FIELD_KEY + objectName;
    }

}
