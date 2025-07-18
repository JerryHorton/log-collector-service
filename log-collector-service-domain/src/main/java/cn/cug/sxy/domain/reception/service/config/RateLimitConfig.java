package cn.cug.sxy.domain.reception.service.config;

import lombok.Getter;

/**
 * 限流配置值对象
 */
@Getter
public class RateLimitConfig {

    // 应用ID
    private final String appId;

    // 端点ID
    private final String endpointId;

    // 限流速率（次/秒）
    private final int rateLimit;

    // 突发容量
    private final int burstCapacity;

    // 是否启用限流
    private final boolean enabled;

    public RateLimitConfig(String appId, String endpointId, int rateLimit, int burstCapacity) {
        this.appId = appId;
        this.endpointId = endpointId;
        this.rateLimit = rateLimit;
        this.burstCapacity = burstCapacity;
        this.enabled = rateLimit > 0;
    }

} 