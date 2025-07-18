package cn.cug.sxy.domain.auth.service.ratelimit;

import cn.cug.sxy.domain.auth.model.valobj.AppId;
import cn.cug.sxy.domain.auth.service.config.RateLimitConfig;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 限流服务
 */
@Slf4j
@Service
public class RateLimitService implements IRateLimitService {

    private final IRateLimiter rateLimiter;

    // 应用限流状态缓存
    private final Map<String, Boolean> rateLimitStatusCache = new ConcurrentHashMap<>();

    // 限流配置缓存
    private final Map<String, RateLimitConfig> rateLimitConfigCache = new ConcurrentHashMap<>();

    public RateLimitService(IRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @PostConstruct
    public void init() {
        // 初始化时不需要做任何事情
        // 当应用请求时，会按需创建限流器
    }

    /**
     * 检查应用是否被限流
     *
     * @param appId 应用ID
     * @return 是否被限流
     */
    public boolean isRateLimited(AppId appId) {
        String key = generateRateLimitKey(appId);

        // 先检查缓存
        Boolean status = rateLimitStatusCache.get(key);
        if (status != null) {
            return status;
        }
        // 尝试获取令牌
        boolean limited = !rateLimiter.tryAcquire(key);
        // 更新缓存
        rateLimitStatusCache.put(key, limited);

        return limited;
    }

    /**
     * 检查应用是否被限流（针对特定端点）
     *
     * @param appId      应用ID
     * @param endpointId 端点ID
     * @return 是否被限流
     */
    public boolean isRateLimited(AppId appId, String endpointId) {
        String key = generateRateLimitKey(appId, endpointId);
        // 先检查缓存
        Boolean status = rateLimitStatusCache.get(key);
        if (status != null) {
            return status;
        }
        // 尝试获取令牌
        boolean limited = !rateLimiter.tryAcquire(key);
        // 更新缓存
        rateLimitStatusCache.put(key, limited);

        return limited;
    }

    /**
     * 检查批量请求是否被限流
     *
     * @param appId 应用ID
     * @param count 请求数量
     * @return 是否被限流
     */
    public boolean isRateLimited(AppId appId, int count) {
        String key = generateRateLimitKey(appId);
        // 尝试获取多个令牌
        boolean limited = !rateLimiter.tryAcquire(key, count);
        // 更新缓存
        rateLimitStatusCache.put(key, limited);

        return limited;
    }

    /**
     * 检查批量请求是否被限流（针对特定端点）
     *
     * @param appId      应用ID
     * @param endpointId 端点ID
     * @param count      请求数量
     * @return 是否被限流
     */
    public boolean isRateLimited(AppId appId, String endpointId, int count) {
        String key = generateRateLimitKey(appId, endpointId);
        // 尝试获取多个令牌
        boolean limited = !rateLimiter.tryAcquire(key, count);
        // 更新缓存
        rateLimitStatusCache.put(key, limited);

        return limited;
    }

    /**
     * 获取应用限流配置
     *
     * @param appId 应用ID
     * @return 限流配置
     */
    @Override
    public RateLimitConfig getRateLimitConfig(AppId appId) {
        String key = generateRateLimitKey(appId);
        // 先检查缓存
        RateLimitConfig config = rateLimitConfigCache.get(key);
        if (config != null) {
            return config;
        }
        // 从限流器获取配置
        int[] rateAndCapacity = rateLimiter.getRateLimiterConfig(key);
        if (rateAndCapacity == null) {
            // 如果没有配置，使用默认值
            return new RateLimitConfig(appId.getValue(), null, 0, 0);
        }
        int rateLimit = rateAndCapacity[0];
        int burstCapacity = rateAndCapacity[1];
        // 创建配置对象
        config = new RateLimitConfig(appId.getValue(), null, rateLimit, burstCapacity);
        // 更新缓存
        rateLimitConfigCache.put(key, config);

        return config;
    }

    /**
     * 获取应用端点限流配置
     *
     * @param appId      应用ID
     * @param endpointId 端点ID
     * @return 限流配置
     */
    @Override
    public RateLimitConfig getRateLimitConfig(AppId appId, String endpointId) {
        String key = generateRateLimitKey(appId, endpointId);
        // 先检查缓存
        RateLimitConfig config = rateLimitConfigCache.get(key);
        if (config != null) {
            return config;
        }
        // 从限流器获取配置
        int[] rateAndCapacity = rateLimiter.getRateLimiterConfig(key);
        if (rateAndCapacity == null) {
            // 如果没有端点级别配置，尝试获取应用级别配置
            return getRateLimitConfig(appId);
        }
        int rateLimit = rateAndCapacity[0];
        int burstCapacity = rateAndCapacity[1];
        // 创建配置对象
        config = new RateLimitConfig(appId.getValue(), endpointId, rateLimit, burstCapacity);
        // 更新缓存
        rateLimitConfigCache.put(key, config);

        return config;
    }

    /**
     * 更新应用限流配置
     *
     * @param appId         应用ID
     * @param rateLimit     限流速率（次/秒）
     * @param burstCapacity 突发容量
     */
    public void updateRateLimit(AppId appId, int rateLimit, int burstCapacity) {
        String key = generateRateLimitKey(appId);
        // 更新限流器配置
        rateLimiter.createOrUpdateRateLimiter(key, rateLimit, burstCapacity);
        // 清除缓存
        rateLimitStatusCache.remove(key);

        log.info("更新应用限流配置: appId={}, rateLimit={}, burstCapacity={}",
                appId.getValue(), rateLimit, burstCapacity);
    }

    /**
     * 更新应用端点限流配置
     *
     * @param appId         应用ID
     * @param endpointId    端点ID
     * @param rateLimit     限流速率（次/秒）
     * @param burstCapacity 突发容量
     */
    public void updateRateLimit(AppId appId, String endpointId, int rateLimit, int burstCapacity) {
        String key = generateRateLimitKey(appId, endpointId);
        // 更新限流器配置
        rateLimiter.createOrUpdateRateLimiter(key, rateLimit, burstCapacity);
        // 清除缓存
        rateLimitStatusCache.remove(key);

        log.info("更新端点限流配置: appId={}, endpointId={}, rateLimit={}, burstCapacity={}",
                appId.getValue(), endpointId, rateLimit, burstCapacity);
    }

    /**
     * 移除应用限流配置
     *
     * @param appId 应用ID
     */
    public void removeRateLimit(AppId appId) {
        String key = generateRateLimitKey(appId);
        // 移除限流器配置
        rateLimiter.removeRateLimiter(key);
        // 清除缓存
        rateLimitStatusCache.remove(key);

        log.info("移除应用限流配置: appId={}", appId.getValue());
    }

    /**
     * 移除应用端点限流配置
     *
     * @param appId      应用ID
     * @param endpointId 端点ID
     */
    public void removeRateLimit(AppId appId, String endpointId) {
        String key = generateRateLimitKey(appId, endpointId);
        // 移除限流器配置
        rateLimiter.removeRateLimiter(key);
        // 清除缓存
        rateLimitStatusCache.remove(key);

        log.info("移除端点限流配置: appId={}, endpointId={}", appId.getValue(), endpointId);
    }

    /**
     * 生成应用限流键
     *
     * @param appId 应用ID
     * @return 限流键
     */
    private String generateRateLimitKey(AppId appId) {
        return "ratelimit:" + appId.getValue();
    }

    /**
     * 生成端点限流键
     *
     * @param appId      应用ID
     * @param endpointId 端点ID
     * @return 限流键
     */
    private String generateRateLimitKey(AppId appId, String endpointId) {
        return "ratelimit:" + appId.getValue() + ":" + endpointId;
    }

} 