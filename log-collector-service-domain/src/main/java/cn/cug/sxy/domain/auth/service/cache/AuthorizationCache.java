package cn.cug.sxy.domain.auth.service.cache;

import cn.cug.sxy.domain.auth.model.valobj.AppId;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @version 1.0
 * @Date 2025/7/10 08:51
 * @Description 授权缓存服务（提供授权结果和限流结果的缓存功能）
 * @Author jerryhotton
 */

@Slf4j
@Component
public class AuthorizationCache {

    // 授权结果缓存
    private final Map<String, CacheEntry<Boolean>> authCache = new ConcurrentHashMap<>();

    // 限流结果缓存
    private final Map<String, CacheEntry<Boolean>> rateLimitCache = new ConcurrentHashMap<>();

    // 缓存有效期（秒）
    private static final long AUTH_CACHE_TTL_SECONDS = 300;
    private static final long RATE_LIMIT_CACHE_TTL_SECONDS = 60;

    // 缓存清理线程
    private final ScheduledExecutorService cleanupExecutor;

    public AuthorizationCache() {
        cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("auth-cache-cleanup");
            t.setDaemon(true);
            return t;
        });

        // 定期清理过期缓存
        cleanupExecutor.scheduleAtFixedRate(
                this::cleanupCache,
                60,
                60,
                TimeUnit.SECONDS);
    }

    /**
     * 获取授权结果，如果缓存中不存在则执行提供的函数并缓存结果
     *
     * @param appId      应用ID
     * @param endpointId 端点ID
     * @param clientIp   客户端IP
     * @param supplier   结果提供者
     * @return 授权结果
     */
    public boolean getAuthorizationResult(String appId, String endpointId, String clientIp,
                                          Supplier<Boolean> supplier) {
        String cacheKey = generateAuthCacheKey(appId, endpointId, clientIp);

        CacheEntry<Boolean> entry = authCache.get(cacheKey);
        if (entry != null && !entry.isExpired()) {
            return entry.getValue();
        }

        // 缓存未命中或已过期，执行实际逻辑
        boolean result = supplier.get();

        // 更新缓存
        authCache.put(cacheKey, new CacheEntry<>(result, AUTH_CACHE_TTL_SECONDS));

        return result;
    }

    /**
     * 获取授权结果，如果缓存中不存在则执行提供的函数并缓存结果
     * 使用自定义的缓存键
     *
     * @param cacheKey 自定义缓存键
     * @param supplier 结果提供者
     * @return 授权结果
     */
    public boolean getAuthorizationResult(String cacheKey, Supplier<Boolean> supplier) {
        CacheEntry<Boolean> entry = authCache.get(cacheKey);
        if (entry != null && !entry.isExpired()) {
            return entry.getValue();
        }

        // 缓存未命中或已过期，执行实际逻辑
        boolean result = supplier.get();

        // 更新缓存
        authCache.put(cacheKey, new CacheEntry<>(result, AUTH_CACHE_TTL_SECONDS));

        return result;
    }

    /**
     * 获取授权结果，如果缓存中不存在则执行提供的函数并缓存结果
     *
     * @param appId      应用ID值对象
     * @param endpointId 端点ID
     * @param clientIp   客户端IP
     * @param supplier   结果提供者
     * @return 授权结果
     */
    public boolean getAuthorizationResult(AppId appId, String endpointId, String clientIp,
                                          Supplier<Boolean> supplier) {
        return getAuthorizationResult(appId.getValue(), endpointId, clientIp, supplier);
    }

    /**
     * 获取限流结果，如果缓存中不存在则执行提供的函数并缓存结果
     *
     * @param appId    应用ID
     * @param supplier 结果提供者
     * @return 限流结果
     */
    public boolean getRateLimitResult(String appId, Supplier<Boolean> supplier) {
        String cacheKey = generateRateLimitCacheKey(appId);

        CacheEntry<Boolean> entry = rateLimitCache.get(cacheKey);
        if (entry != null && !entry.isExpired()) {
            return entry.getValue();
        }

        // 缓存未命中或已过期，执行实际逻辑
        boolean result = supplier.get();

        // 更新缓存
        rateLimitCache.put(cacheKey, new CacheEntry<>(result, RATE_LIMIT_CACHE_TTL_SECONDS));

        return result;
    }

    /**
     * 获取限流结果，如果缓存中不存在则执行提供的函数并缓存结果
     *
     * @param appId    应用ID值对象
     * @param supplier 结果提供者
     * @return 限流结果
     */
    public boolean getRateLimitResult(AppId appId, Supplier<Boolean> supplier) {
        return getRateLimitResult(appId.getValue(), supplier);
    }

    /**
     * 清理过期缓存
     */
    private void cleanupCache() {
        try {
            int authExpiredCount = 0;
            int rateLimitExpiredCount = 0;
            // 清理授权缓存
            for (Map.Entry<String, CacheEntry<Boolean>> entry : authCache.entrySet()) {
                if (entry.getValue().isExpired()) {
                    authCache.remove(entry.getKey());
                    authExpiredCount++;
                }
            }
            // 清理限流缓存
            for (Map.Entry<String, CacheEntry<Boolean>> entry : rateLimitCache.entrySet()) {
                if (entry.getValue().isExpired()) {
                    rateLimitCache.remove(entry.getKey());
                    rateLimitExpiredCount++;
                }
            }
            if (authExpiredCount > 0 || rateLimitExpiredCount > 0) {
                log.debug("清理过期缓存: 授权缓存={}, 限流缓存={}",
                        authExpiredCount, rateLimitExpiredCount);
            }
        } catch (Exception e) {
            log.error("清理缓存时发生错误", e);
        }
    }

    /**
     * 生成授权缓存键
     */
    private String generateAuthCacheKey(String appId, String endpointId, String clientIp) {
        return "auth:" + appId + ":" + endpointId + ":" + clientIp;
    }

    /**
     * 生成授权缓存键
     */
    private String generateAuthCacheKey(AppId appId, String endpointId, String clientIp) {
        return generateAuthCacheKey(appId.getValue(), endpointId, clientIp);
    }

    /**
     * 生成限流缓存键
     */
    private String generateRateLimitCacheKey(String appId) {
        return "ratelimit:" + appId;
    }

    /**
     * 生成限流缓存键
     */
    private String generateRateLimitCacheKey(AppId appId) {
        return generateRateLimitCacheKey(appId.getValue());
    }

    /**
     * 缓存条目，包含值和过期时间
     */
    @Getter
    private static class CacheEntry<T> {
        private final T value;
        private final long expiryTime;

        public CacheEntry(T value, long ttlSeconds) {
            this.value = value;
            this.expiryTime = System.currentTimeMillis() + (ttlSeconds * 1000);
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

}
