package cn.cug.sxy.domain.reception.service.metrics;

import cn.cug.sxy.domain.reception.model.valobj.AppId;
import cn.cug.sxy.domain.reception.model.valobj.EndpointId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * 日志处理监控指标收集器
 */
@Slf4j
@Component
public class LogProcessingMetrics {
    
    // 接收日志计数器 (按应用ID和端点ID)
    private final Map<String, LongAdder> receivedLogCounter = new ConcurrentHashMap<>();
    
    // 接收批次计数器 (按应用ID和端点ID)
    private final Map<String, LongAdder> receivedBatchCounter = new ConcurrentHashMap<>();
    
    // 处理成功计数器 (按应用ID和端点ID)
    private final Map<String, LongAdder> processedSuccessCounter = new ConcurrentHashMap<>();
    
    // 处理失败计数器 (按应用ID和端点ID)
    private final Map<String, LongAdder> processedFailureCounter = new ConcurrentHashMap<>();
    
    // 重试计数器 (按应用ID和端点ID)
    private final Map<String, LongAdder> retryCounter = new ConcurrentHashMap<>();
    
    // 处理耗时统计 (按应用ID和端点ID)
    private final Map<String, AtomicLong> processingTimeTotal = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> processingTimeMax = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> processingTimeMin = new ConcurrentHashMap<>();
    private final Map<String, LongAdder> processingTimeCount = new ConcurrentHashMap<>();
    
    /**
     * 生成指标键
     */
    private String getMetricKey(AppId appId, EndpointId endpointId) {
        return appId.getValue() + ":" + endpointId.getValue();
    }
    
    /**
     * 记录接收日志
     */
    public void recordLogReceived(AppId appId, EndpointId endpointId, int count) {
        String key = getMetricKey(appId, endpointId);
        receivedLogCounter.computeIfAbsent(key, k -> new LongAdder()).add(count);
    }
    
    /**
     * 记录接收批次
     */
    public void recordBatchReceived(AppId appId, EndpointId endpointId) {
        String key = getMetricKey(appId, endpointId);
        receivedBatchCounter.computeIfAbsent(key, k -> new LongAdder()).increment();
    }
    
    /**
     * 记录处理成功
     */
    public void recordProcessSuccess(AppId appId, EndpointId endpointId, int logCount, long processingTimeMs) {
        String key = getMetricKey(appId, endpointId);
        processedSuccessCounter.computeIfAbsent(key, k -> new LongAdder()).add(logCount);
        
        // 记录处理耗时
        recordProcessingTime(key, processingTimeMs);
    }
    
    /**
     * 记录处理失败
     */
    public void recordProcessFailure(AppId appId, EndpointId endpointId) {
        String key = getMetricKey(appId, endpointId);
        processedFailureCounter.computeIfAbsent(key, k -> new LongAdder()).increment();
    }
    
    /**
     * 记录重试
     */
    public void recordRetry(AppId appId, EndpointId endpointId) {
        String key = getMetricKey(appId, endpointId);
        retryCounter.computeIfAbsent(key, k -> new LongAdder()).increment();
    }
    
    /**
     * 记录处理耗时
     */
    private void recordProcessingTime(String key, long processingTimeMs) {
        // 累计总耗时
        processingTimeTotal.computeIfAbsent(key, k -> new AtomicLong(0))
                .addAndGet(processingTimeMs);
        
        // 更新最大耗时
        processingTimeMax.computeIfAbsent(key, k -> new AtomicLong(0))
                .updateAndGet(current -> Math.max(current, processingTimeMs));
        
        // 更新最小耗时
        AtomicLong min = processingTimeMin.computeIfAbsent(key, k -> new AtomicLong(Long.MAX_VALUE));
        long currentMin;
        do {
            currentMin = min.get();
            if (processingTimeMs >= currentMin) {
                break;
            }
        } while (!min.compareAndSet(currentMin, processingTimeMs));
        
        // 增加计数
        processingTimeCount.computeIfAbsent(key, k -> new LongAdder()).increment();
    }
    
    /**
     * 获取接收日志计数
     */
    public long getReceivedLogCount(AppId appId, EndpointId endpointId) {
        String key = getMetricKey(appId, endpointId);
        LongAdder counter = receivedLogCounter.get(key);
        return counter != null ? counter.sum() : 0;
    }
    
    /**
     * 获取接收批次计数
     */
    public long getReceivedBatchCount(AppId appId, EndpointId endpointId) {
        String key = getMetricKey(appId, endpointId);
        LongAdder counter = receivedBatchCounter.get(key);
        return counter != null ? counter.sum() : 0;
    }
    
    /**
     * 获取处理成功计数
     */
    public long getProcessedSuccessCount(AppId appId, EndpointId endpointId) {
        String key = getMetricKey(appId, endpointId);
        LongAdder counter = processedSuccessCounter.get(key);
        return counter != null ? counter.sum() : 0;
    }
    
    /**
     * 获取处理失败计数
     */
    public long getProcessedFailureCount(AppId appId, EndpointId endpointId) {
        String key = getMetricKey(appId, endpointId);
        LongAdder counter = processedFailureCounter.get(key);
        return counter != null ? counter.sum() : 0;
    }
    
    /**
     * 获取重试计数
     */
    public long getRetryCount(AppId appId, EndpointId endpointId) {
        String key = getMetricKey(appId, endpointId);
        LongAdder counter = retryCounter.get(key);
        return counter != null ? counter.sum() : 0;
    }
    
    /**
     * 获取平均处理耗时
     */
    public double getAverageProcessingTime(AppId appId, EndpointId endpointId) {
        String key = getMetricKey(appId, endpointId);
        AtomicLong total = processingTimeTotal.get(key);
        LongAdder count = processingTimeCount.get(key);
        
        if (total == null || count == null || count.sum() == 0) {
            return 0;
        }
        
        return (double) total.get() / count.sum();
    }
    
    /**
     * 获取最大处理耗时
     */
    public long getMaxProcessingTime(AppId appId, EndpointId endpointId) {
        String key = getMetricKey(appId, endpointId);
        AtomicLong max = processingTimeMax.get(key);
        return max != null ? max.get() : 0;
    }
    
    /**
     * 获取最小处理耗时
     */
    public long getMinProcessingTime(AppId appId, EndpointId endpointId) {
        String key = getMetricKey(appId, endpointId);
        AtomicLong min = processingTimeMin.get(key);
        return min != null ? min.get() : 0;
    }
    
    /**
     * 重置指标
     */
    public void reset() {
        receivedLogCounter.clear();
        receivedBatchCounter.clear();
        processedSuccessCounter.clear();
        processedFailureCounter.clear();
        retryCounter.clear();
        processingTimeTotal.clear();
        processingTimeMax.clear();
        processingTimeMin.clear();
        processingTimeCount.clear();
    }

} 