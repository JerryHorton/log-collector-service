package cn.cug.sxy.domain.reception.service.cache;

import cn.cug.sxy.domain.reception.model.valobj.AppId;
import cn.cug.sxy.domain.reception.model.valobj.EndpointId;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 授权结果缓存
 * 用于缓存应用对端点的访问权限结果，避免频繁查询数据库
 */
@Component
public class AuthorizationCache {

    /**
     * 缓存项
     */
    private static class CacheItem {
        private final boolean result;
        private final Instant expireTime;

        public CacheItem(boolean result, Duration ttl) {
            this.result = result;
            this.expireTime = Instant.now().plus(ttl);
        }

        public boolean isExpired() {
            return Instant.now().isAfter(expireTime);
        }

        public boolean getResult() {
            return result;
        }
    }

    /**
     * 默认缓存有效期（60秒）
     */
    private static final Duration DEFAULT_TTL = Duration.ofSeconds(60);

    /**
     * 授权结果缓存
     * key: appId:endpointId:clientIp
     * value: 缓存项
     */
    private final Map<String, CacheItem> authCache = new ConcurrentHashMap<>();

    /**
     * 限流结果缓存
     * key: appId
     * value: 缓存项
     */
    private final Map<String, CacheItem> rateLimitCache = new ConcurrentHashMap<>();

    /**
     * 获取授权结果，如果缓存中不存在或已过期，则调用supplier获取结果并缓存
     *
     * @param appId 应用ID
     * @param endpointId 端点ID
     * @param clientIp 客户端IP
     * @param supplier 结果提供者
     * @return 授权结果
     */
    public boolean getAuthorizationResult(AppId appId, EndpointId endpointId, String clientIp, 
                                        Supplier<Boolean> supplier) {
        return getAuthorizationResult(appId, endpointId, clientIp, supplier, DEFAULT_TTL);
    }

    /**
     * 获取授权结果，如果缓存中不存在或已过期，则调用supplier获取结果并缓存
     *
     * @param appId 应用ID
     * @param endpointId 端点ID
     * @param clientIp 客户端IP
     * @param supplier 结果提供者
     * @param ttl 缓存有效期
     * @return 授权结果
     */
    public boolean getAuthorizationResult(AppId appId, EndpointId endpointId, String clientIp, 
                                        Supplier<Boolean> supplier, Duration ttl) {
        String key = generateAuthKey(appId, endpointId, clientIp);
        CacheItem item = authCache.get(key);
        
        // 如果缓存不存在或已过期，则重新获取结果
        if (item == null || item.isExpired()) {
            boolean result = supplier.get();
            authCache.put(key, new CacheItem(result, ttl));
            return result;
        }
        
        return item.getResult();
    }

    /**
     * 获取限流结果，如果缓存中不存在或已过期，则调用supplier获取结果并缓存
     *
     * @param appId 应用ID
     * @param supplier 结果提供者
     * @return 限流结果
     */
    public boolean getRateLimitResult(AppId appId, Supplier<Boolean> supplier) {
        return getRateLimitResult(appId, supplier, DEFAULT_TTL);
    }

    /**
     * 获取限流结果，如果缓存中不存在或已过期，则调用supplier获取结果并缓存
     *
     * @param appId 应用ID
     * @param supplier 结果提供者
     * @param ttl 缓存有效期
     * @return 限流结果
     */
    public boolean getRateLimitResult(AppId appId, Supplier<Boolean> supplier, Duration ttl) {
        String key = appId.getValue();
        CacheItem item = rateLimitCache.get(key);
        
        // 如果缓存不存在或已过期，则重新获取结果
        if (item == null || item.isExpired()) {
            boolean result = supplier.get();
            rateLimitCache.put(key, new CacheItem(result, ttl));
            return result;
        }
        
        return item.getResult();
    }

    /**
     * 清除授权结果缓存
     *
     * @param appId 应用ID
     * @param endpointId 端点ID
     * @param clientIp 客户端IP
     */
    public void invalidateAuthorizationCache(AppId appId, EndpointId endpointId, String clientIp) {
        String key = generateAuthKey(appId, endpointId, clientIp);
        authCache.remove(key);
    }

    /**
     * 清除限流结果缓存
     *
     * @param appId 应用ID
     */
    public void invalidateRateLimitCache(AppId appId) {
        rateLimitCache.remove(appId.getValue());
    }

    /**
     * 生成授权缓存键
     */
    private String generateAuthKey(AppId appId, EndpointId endpointId, String clientIp) {
        return appId.getValue() + ":" + endpointId.getValue() + ":" + (clientIp == null ? "" : clientIp);
    }
} 