package cn.cug.sxy.domain.reception.service;

import cn.cug.sxy.domain.reception.model.valobj.AppId;
import cn.cug.sxy.domain.reception.model.valobj.EndpointId;
import cn.cug.sxy.domain.reception.service.config.RateLimitConfig;
import cn.cug.sxy.domain.reception.service.config.ReceptionConfigService;
import cn.cug.sxy.domain.reception.service.ratelimit.IRateLimiter;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 限流服务
 */

public interface IRateLimitService {
    
    /**
     * 检查应用是否被限流
     * 
     * @param appId 应用ID
     * @param endpointId 端点ID
     * @return 是否被限流
     */
    boolean isRateLimited(AppId appId, EndpointId endpointId);
    
    /**
     * 检查批量请求是否被限流
     * 
     * @param appId 应用ID
     * @param endpointId 端点ID
     * @param count 请求数量
     * @return 是否被限流
     */
    boolean isRateLimited(AppId appId, EndpointId endpointId, int count);
    
    /**
     * 更新限流配置
     * 
     * @param appId 应用ID
     * @param endpointId 端点ID
     * @param rateLimit 限流速率（次/秒）
     * @param burstCapacity 突发容量
     */
    void updateRateLimit(AppId appId, EndpointId endpointId, int rateLimit, int burstCapacity);
    
    /**
     * 移除限流配置
     * 
     * @param appId 应用ID
     * @param endpointId 端点ID
     */
    void removeRateLimit(AppId appId, EndpointId endpointId);

} 