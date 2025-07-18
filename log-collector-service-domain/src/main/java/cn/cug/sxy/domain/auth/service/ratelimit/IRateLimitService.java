package cn.cug.sxy.domain.auth.service.ratelimit;

import cn.cug.sxy.domain.auth.model.valobj.AppId;
import cn.cug.sxy.domain.auth.service.config.RateLimitConfig;
import cn.cug.sxy.domain.reception.model.valobj.EndpointId;

/**
 * 限流服务
 */

public interface IRateLimitService {

    /**
     * 检查应用是否被限流
     *
     * @param appId 应用ID
     * @return 是否被限流
     */
    boolean isRateLimited(AppId appId);

    /**
     * 检查应用是否被限流（针对特定端点）
     *
     * @param appId 应用ID
     * @param endpointId 端点ID
     * @return 是否被限流
     */
    boolean isRateLimited(AppId appId, String endpointId);

    /**
     * 检查批量请求是否被限流
     *
     * @param appId 应用ID
     * @param count 请求数量
     * @return 是否被限流
     */
    boolean isRateLimited(AppId appId, int count);

    /**
     * 检查批量请求是否被限流（针对特定端点）
     *
     * @param appId 应用ID
     * @param endpointId 端点ID
     * @param count 请求数量
     * @return 是否被限流
     */
    boolean isRateLimited(AppId appId, String endpointId, int count);

    /**
     * 获取应用限流配置
     *
     * @param appId 应用ID
     * @return 限流配置
     */
    RateLimitConfig getRateLimitConfig(AppId appId);

    /**
     * 获取应用端点限流配置
     *
     * @param appId 应用ID
     * @param endpointId 端点ID
     * @return 限流配置
     */
    RateLimitConfig getRateLimitConfig(AppId appId, String endpointId);

    /**
     * 更新应用限流配置
     *
     * @param appId 应用ID
     * @param rateLimit 限流速率（次/秒）
     * @param burstCapacity 突发容量
     */
    void updateRateLimit(AppId appId, int rateLimit, int burstCapacity);

    /**
     * 更新应用端点限流配置
     *
     * @param appId 应用ID
     * @param endpointId 端点ID
     * @param rateLimit 限流速率（次/秒）
     * @param burstCapacity 突发容量
     */
    void updateRateLimit(AppId appId, String endpointId, int rateLimit, int burstCapacity);

    /**
     * 移除应用限流配置
     *
     * @param appId 应用ID
     */
    void removeRateLimit(AppId appId);

    /**
     * 移除应用端点限流配置
     *
     * @param appId 应用ID
     * @param endpointId 端点ID
     */
    void removeRateLimit(AppId appId, String endpointId);

} 