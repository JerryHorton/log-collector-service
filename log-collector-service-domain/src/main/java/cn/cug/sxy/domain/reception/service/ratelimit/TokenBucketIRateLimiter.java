package cn.cug.sxy.domain.reception.service.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 令牌桶限流器实现
 */
@Slf4j
@Component
public class TokenBucketIRateLimiter implements IRateLimiter {
    
    // 令牌桶映射
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    
    // 令牌生成调度器
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    // 令牌生成间隔（毫秒）
    private static final long TOKEN_GENERATION_INTERVAL_MS = 100;
    
    public TokenBucketIRateLimiter() {
        // 启动令牌生成任务
        scheduler.scheduleAtFixedRate(
                this::generateTokens,
                TOKEN_GENERATION_INTERVAL_MS,
                TOKEN_GENERATION_INTERVAL_MS,
                TimeUnit.MILLISECONDS
        );
    }
    
    @Override
    public boolean tryAcquire(String key) {
        return tryAcquire(key, 1);
    }
    
    @Override
    public boolean tryAcquire(String key, int tokens) {
        TokenBucket bucket = buckets.get(key);
        if (bucket == null) {
            // 如果没有配置限流，则默认允许通过
            return true;
        }
        
        return bucket.tryConsume(tokens);
    }
    
    @Override
    public void createOrUpdateRateLimiter(String key, int rate, int burstCapacity) {
        if (rate <= 0 || burstCapacity <= 0) {
            // 如果速率或容量小于等于0，则移除限流器
            removeRateLimiter(key);
            return;
        }
        
        // 计算每毫秒生成的令牌数
        double tokensPerMs = rate / 1000.0;
        
        // 创建或更新令牌桶
        buckets.put(key, new TokenBucket(tokensPerMs, burstCapacity));
        log.info("创建/更新限流器: key={}, rate={}, burstCapacity={}", key, rate, burstCapacity);
    }
    
    @Override
    public void removeRateLimiter(String key) {
        buckets.remove(key);
        log.info("移除限流器: key={}", key);
    }
    
    /**
     * 为所有令牌桶生成令牌
     */
    private void generateTokens() {
        try {
            long currentTime = System.currentTimeMillis();
            
            for (Map.Entry<String, TokenBucket> entry : buckets.entrySet()) {
                entry.getValue().generateTokens(currentTime);
            }
        } catch (Exception e) {
            log.error("生成令牌异常", e);
        }
    }
    
    /**
     * 令牌桶实现
     */
    private static class TokenBucket {
        // 每毫秒生成的令牌数
        private final double tokensPerMs;
        
        // 桶容量
        private final int capacity;
        
        // 当前令牌数
        private double tokens;
        
        // 上次生成令牌的时间
        private long lastRefillTime;
        
        public TokenBucket(double tokensPerMs, int capacity) {
            this.tokensPerMs = tokensPerMs;
            this.capacity = capacity;
            this.tokens = capacity; // 初始时桶是满的
            this.lastRefillTime = System.currentTimeMillis();
        }
        
        /**
         * 尝试消费令牌
         * 
         * @param count 令牌数量
         * @return 是否消费成功
         */
        public synchronized boolean tryConsume(int count) {
            if (count <= tokens) {
                tokens -= count;
                return true;
            }
            return false;
        }
        
        /**
         * 生成令牌
         * 
         * @param currentTime 当前时间
         */
        public synchronized void generateTokens(long currentTime) {
            if (currentTime <= lastRefillTime) {
                return;
            }
            
            // 计算经过的时间
            long elapsedTime = currentTime - lastRefillTime;
            
            // 计算需要生成的令牌数
            double newTokens = elapsedTime * tokensPerMs;
            
            // 更新令牌数，不超过容量
            tokens = Math.min(capacity, tokens + newTokens);
            
            // 更新上次生成时间
            lastRefillTime = currentTime;
        }
    }

} 