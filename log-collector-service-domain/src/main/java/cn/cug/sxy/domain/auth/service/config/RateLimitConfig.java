package cn.cug.sxy.domain.auth.service.config;

import lombok.Getter;

/**
 * @version 1.0
 * @Date 2025/7/10 11:22
 * @Description 限流配置值对象
 * @Author jerryhotton
 */

@Getter
public class RateLimitConfig {

    // 应用ID
    private final String appId;

    // 端点ID，可为null表示应用级别限流
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
