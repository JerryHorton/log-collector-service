package cn.cug.sxy.domain.reception.service.config;

import cn.cug.sxy.domain.reception.adapter.repository.IReceiverEndpointRepository;
import cn.cug.sxy.domain.reception.model.aggregate.ReceiverEndpoint;
import cn.cug.sxy.domain.reception.model.valobj.AppId;
import cn.cug.sxy.domain.reception.model.valobj.EndpointId;
import cn.cug.sxy.domain.reception.model.valobj.EndpointStatus;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 接收配置管理服务
 * 负责管理接收端点配置、限流配置等
 */
@Slf4j
@Service
public class ReceptionConfigService {

    private final IReceiverEndpointRepository endpointRepository;
    
    // 缓存端点配置
    private final Map<EndpointId, EndpointConfig> endpointConfigCache = new ConcurrentHashMap<>();
    
    // 缓存应用-端点限流配置
    private final Map<String, RateLimitConfig> rateLimitConfigCache = new ConcurrentHashMap<>();
    
    // 配置刷新调度器
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    // 配置刷新间隔（秒）
    private static final long CONFIG_REFRESH_INTERVAL_SECONDS = 60;
    
    public ReceptionConfigService(IReceiverEndpointRepository endpointRepository) {
        this.endpointRepository = endpointRepository;
    }
    
    @PostConstruct
    public void init() {
        // 初始加载所有配置
        refreshAllConfigs();
        
        // 定时刷新配置
        scheduler.scheduleAtFixedRate(
                this::refreshAllConfigs,
                CONFIG_REFRESH_INTERVAL_SECONDS,
                CONFIG_REFRESH_INTERVAL_SECONDS,
                TimeUnit.SECONDS
        );
    }
    
    /**
     * 刷新所有配置
     */
    public void refreshAllConfigs() {
        try {
            log.info("开始刷新接收配置...");
            
            // 刷新端点配置
            List<ReceiverEndpoint> endpoints = endpointRepository.findAll();
            for (ReceiverEndpoint endpoint : endpoints) {
                EndpointConfig config = new EndpointConfig(
                        endpoint.getAppId(),
                        endpoint.getName(),
                        endpoint.getFormat(),
                        endpoint.getStatus(),
                        endpoint.getMaxPayloadSize(),
                        calculateMaxBatchSize(endpoint),
                        endpoint.getMaxBatchCount()
                );
                endpointConfigCache.put(endpoint.getAppId(), config);
                
                // 刷新该端点的应用限流配置
                refreshRateLimitConfigs(endpoint);
            }
            
            log.info("接收配置刷新完成，共加载{}个端点配置", endpointConfigCache.size());
        } catch (Exception e) {
            log.error("刷新接收配置异常", e);
        }
    }
    
    /**
     * 刷新端点的应用限流配置
     */
    private void refreshRateLimitConfigs(ReceiverEndpoint endpoint) {
        // 获取端点的所有应用访问权限
        endpoint.getAppAccesses().forEach(appAccess -> {
            String key = generateRateLimitKey(appAccess.getAppId(), endpoint.getAppId());
            RateLimitConfig config = new RateLimitConfig(
                    appAccess.getAppId(),
                    endpoint.getAppId(),
                    appAccess.getRateLimit(),
                    appAccess.getBurstCapacity()
            );
            rateLimitConfigCache.put(key, config);
        });
    }
    
    /**
     * 获取端点配置
     * 
     * @param endpointId 端点ID
     * @return 端点配置，如果不存在则返回null
     */
    public EndpointConfig getEndpointConfig(EndpointId endpointId) {
        EndpointConfig config = endpointConfigCache.get(endpointId);
        
        // 如果缓存中不存在，则从数据库加载
        if (config == null) {
            Optional<ReceiverEndpoint> endpointOpt = endpointRepository.findById(endpointId);
            if (endpointOpt.isPresent()) {
                ReceiverEndpoint endpoint = endpointOpt.get();
                config = new EndpointConfig(
                        endpoint.getAppId(),
                        endpoint.getName(),
                        endpoint.getFormat(),
                        endpoint.getStatus(),
                        endpoint.getMaxPayloadSize(),
                        calculateMaxBatchSize(endpoint),
                        endpoint.getMaxBatchCount()
                );
                endpointConfigCache.put(endpointId, config);
                
                // 同时刷新该端点的应用限流配置
                refreshRateLimitConfigs(endpoint);
            }
        }
        
        return config;
    }
    
    /**
     * 获取应用-端点限流配置
     * 
     * @param appId 应用ID
     * @param endpointId 端点ID
     * @return 限流配置，如果不存在则返回null
     */
    public RateLimitConfig getRateLimitConfig(AppId appId, EndpointId endpointId) {
        String key = generateRateLimitKey(appId, endpointId);
        return rateLimitConfigCache.get(key);
    }
    
    /**
     * 检查端点是否活跃
     * 
     * @param endpointId 端点ID
     * @return 是否活跃
     */
    public boolean isEndpointActive(EndpointId endpointId) {
        EndpointConfig config = getEndpointConfig(endpointId);
        return config != null && config.getStatus() == EndpointStatus.ACTIVE;
    }
    
    /**
     * 计算最大批次大小
     * 
     * @param endpoint 端点
     * @return 最大批次大小
     */
    private int calculateMaxBatchSize(ReceiverEndpoint endpoint) {
        // 默认为单条最大负载的100倍
        return endpoint.getMaxPayloadSize() * 100;
    }
    
    /**
     * 生成限流配置键
     * 
     * @param appId 应用ID
     * @param endpointId 端点ID
     * @return 限流配置键
     */
    private String generateRateLimitKey(AppId appId, EndpointId endpointId) {
        return appId.getValue() + ":" + endpointId.getValue();
    }
} 