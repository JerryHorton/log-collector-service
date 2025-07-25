package cn.cug.sxy.domain.auth.service.ratelimit;

/**
 * 限流器接口
 */
public interface IRateLimiter {
    
    /**
     * 尝试获取令牌
     * 
     * @param key 限流键
     * @return 是否获取成功
     */
    boolean tryAcquire(String key);
    
    /**
     * 尝试获取多个令牌
     * 
     * @param key 限流键
     * @param tokens 令牌数量
     * @return 是否获取成功
     */
    boolean tryAcquire(String key, int tokens);

    /**
     * 获取限流器配置
     *
     * @param key 限流键
     * @return 配置数组[速率, 突发容量]，如果不存在则返回null
     */
    int[] getRateLimiterConfig(String key);
    
    /**
     * 创建或更新限流器配置
     * 
     * @param key 限流键
     * @param rate 速率（次/秒）
     * @param burstCapacity 突发容量
     */
    void createOrUpdateRateLimiter(String key, int rate, int burstCapacity);
    
    /**
     * 移除限流器配置
     * 
     * @param key 限流键
     */
    void removeRateLimiter(String key);

} 