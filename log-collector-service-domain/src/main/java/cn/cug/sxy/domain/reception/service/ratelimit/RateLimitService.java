package cn.cug.sxy.domain.reception.service.ratelimit;

import cn.cug.sxy.domain.reception.model.valobj.AppId;
import cn.cug.sxy.domain.reception.model.valobj.EndpointId;
import cn.cug.sxy.domain.reception.service.IRateLimitService;
import cn.cug.sxy.domain.reception.service.config.RateLimitConfig;
import cn.cug.sxy.domain.reception.service.config.ReceptionConfigService;
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
    
    private final IRateLimiter IRateLimiter;
    private final ReceptionConfigService configService;
    
    // 应用-端点限流状态缓存
    private final Map<String, Boolean> rateLimitStatusCache = new ConcurrentHashMap<>();
    
    public RateLimitService(IRateLimiter IRateLimiter, ReceptionConfigService configService) {
        this.IRateLimiter = IRateLimiter;
        this.configService = configService;
    }
    
    @PostConstruct
    public void init() {
        // 初始化时不需要做任何事情，配置服务会自动加载配置
        // 当应用请求时，会按需创建限流器
    }
    
    /**
     * 检查应用是否被限流
     * 
     * @param appId 应用ID
     * @param endpointId 端点ID
     * @return 是否被限流
     */
    public boolean isRateLimited(AppId appId, EndpointId endpointId) {
        String key = generateRateLimitKey(appId, endpointId);
        
        // 先检查缓存
        Boolean status = rateLimitStatusCache.get(key);
        if (status != null) {
            return status;
        }
        
        // 获取限流配置
        RateLimitConfig config = configService.getRateLimitConfig(appId, endpointId);
        
        // 如果没有配置或未启用限流，则不限流
        if (config == null || !config.isEnabled()) {
            rateLimitStatusCache.put(key, false);
            return false;
        }
        
        // 尝试获取令牌
        boolean limited = !IRateLimiter.tryAcquire(key);
        
        // 更新缓存
        rateLimitStatusCache.put(key, limited);
        
        return limited;
    }
    
    /**
     * 检查批量请求是否被限流
     * 
     * @param appId 应用ID
     * @param endpointId 端点ID
     * @param count 请求数量
     * @return 是否被限流
     */
    public boolean isRateLimited(AppId appId, EndpointId endpointId, int count) {
        String key = generateRateLimitKey(appId, endpointId);
        
        // 获取限流配置
        RateLimitConfig config = configService.getRateLimitConfig(appId, endpointId);
        
        // 如果没有配置或未启用限流，则不限流
        if (config == null || !config.isEnabled()) {
            return false;
        }
        
        // 尝试获取多个令牌
        boolean limited = !IRateLimiter.tryAcquire(key, count);
        
        // 更新缓存
        rateLimitStatusCache.put(key, limited);
        
        return limited;
    }
    
    /**
     * 更新限流配置
     * 
     * @param appId 应用ID
     * @param endpointId 端点ID
     * @param rateLimit 限流速率（次/秒）
     * @param burstCapacity 突发容量
     */
    public void updateRateLimit(AppId appId, EndpointId endpointId, int rateLimit, int burstCapacity) {
        String key = generateRateLimitKey(appId, endpointId);
        
        // 更新限流器配置
        IRateLimiter.createOrUpdateRateLimiter(key, rateLimit, burstCapacity);
        
        // 清除缓存
        rateLimitStatusCache.remove(key);
        
        log.info("更新限流配置: appId={}, endpointId={}, rateLimit={}, burstCapacity={}",
                appId.getValue(), endpointId.getValue(), rateLimit, burstCapacity);
    }
    
    /**
     * 移除限流配置
     * 
     * @param appId 应用ID
     * @param endpointId 端点ID
     */
    public void removeRateLimit(AppId appId, EndpointId endpointId) {
        String key = generateRateLimitKey(appId, endpointId);
        
        // 移除限流器配置
        IRateLimiter.removeRateLimiter(key);
        
        // 清除缓存
        rateLimitStatusCache.remove(key);
        
        log.info("移除限流配置: appId={}, endpointId={}", appId.getValue(), endpointId.getValue());
    }
    
    /**
     * 生成限流键
     * 
     * @param appId 应用ID
     * @param endpointId 端点ID
     * @return 限流键
     */
    private String generateRateLimitKey(AppId appId, EndpointId endpointId) {
        return "ratelimit:" + appId.getValue() + ":" + endpointId.getValue();
    }

} 