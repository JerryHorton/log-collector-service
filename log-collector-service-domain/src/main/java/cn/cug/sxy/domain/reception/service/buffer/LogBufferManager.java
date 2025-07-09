package cn.cug.sxy.domain.reception.service.buffer;

import cn.cug.sxy.domain.reception.adapter.repository.ILogBatchRepository;
import cn.cug.sxy.domain.reception.model.entity.LogBatch;
import cn.cug.sxy.domain.reception.model.valobj.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * @version 1.0
 * @Date 2025/7/8 19:03
 * @Description 日志缓冲区管理器（用于聚合短时间内的单条日志，提高性能）
 * @Author jerryhotton
 */

@Slf4j
@Component
public class LogBufferManager implements InitializingBean, DisposableBean {

    /**
     * 缓冲区刷新间隔（毫秒）
     */
    private static final long FLUSH_INTERVAL_MS = 1000;

    /**
     * 缓冲区大小阈值，达到该值时触发刷新
     */
    private static final int BUFFER_THRESHOLD = 100;

    /**
     * 最大缓冲时间（毫秒），超过该时间强制刷新
     */
    private static final long MAX_BUFFER_TIME_MS = 5000;

    /**
     * 日志缓冲区
     * key: appId:endpointId
     * value: 缓冲区对象
     */
    private final Map<String, LogBuffer> buffers = new ConcurrentHashMap<>();

    /**
     * 定时任务执行器
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, r -> {
        Thread t = new Thread(r, "log-buffer-flusher");
        t.setDaemon(true);
        return t;
    });

    /**
     * 日志批次仓储
     */
    private final ILogBatchRepository logBatchRepository;

    /**
     * 批次处理回调
     */
    @Setter
    private Consumer<BatchId> batchProcessCallback;

    public LogBufferManager(ILogBatchRepository logBatchRepository) {
        this.logBatchRepository = logBatchRepository;
    }

    /**
     * 添加日志到缓冲区
     *
     * @param rawLog     原始日志
     * @param appId      应用ID
     * @param endpointId 端点ID
     * @return 是否触发了刷新
     */
    public boolean addLog(RawLog rawLog, AppId appId, EndpointId endpointId) {
        String key = generateBufferKey(appId, endpointId);

        // 获取或创建缓冲区
        LogBuffer buffer = buffers.computeIfAbsent(key, k ->
                new LogBuffer(appId, endpointId, Instant.now()));
        // 添加日志
        buffer.addLog(rawLog);
        // 检查是否达到阈值
        if (buffer.size() >= BUFFER_THRESHOLD ||
                buffer.getCreationTime().plusMillis(MAX_BUFFER_TIME_MS).isBefore(Instant.now())) {
            // 异步刷新缓冲区
            CompletableFuture.runAsync(() -> flushBuffer(key));
            return true;
        }

        return false;
    }

    /**
     * 刷新所有缓冲区
     */
    public void flushAllBuffers() {
        log.debug("开始刷新所有日志缓冲区, 缓冲区数量: {}", buffers.size());

        // 复制键集合，避免并发修改异常
        List<String> keys = new ArrayList<>(buffers.keySet());

        for (String key : keys) {
            flushBuffer(key);
        }
    }

    /**
     * 刷新指定缓冲区
     *
     * @param key 缓冲区键
     */
    public void flushBuffer(String key) {
        // 原子移除缓冲区
        LogBuffer buffer = buffers.remove(key);
        if (buffer == null || buffer.isEmpty()) {
            return;
        }
        try {
            List<RawLog> logs = buffer.getLogs();
            AppId appId = buffer.getAppId();
            EndpointId endpointId = buffer.getEndpointId();
            log.debug("刷新日志缓冲区: appId={}, endpointId={}, logCount={}",
                    appId.getValue(), endpointId.getValue(), logs.size());
            // 创建批次
            BatchId batchId = BatchId.generate();
            LogBatch batch = new LogBatch(
                    batchId,
                    appId,
                    endpointId,
                    new ArrayList<>(logs),
                    BatchStatus.PENDING,
                    Instant.now()
            );
            // 保存批次
            logBatchRepository.save(batch);
            // 触发批次处理回调
            if (batchProcessCallback != null) {
                batchProcessCallback.accept(batchId);
            }
        } catch (Exception e) {
            log.error("刷新日志缓冲区异常: key={}", key, e);
        }
    }

    @Override
    public void afterPropertiesSet() {
        // 启动定时刷新任务
        scheduler.scheduleAtFixedRate(
                this::flushAllBuffers,
                FLUSH_INTERVAL_MS,
                FLUSH_INTERVAL_MS,
                TimeUnit.MILLISECONDS);

        log.info("日志缓冲区管理器已启动, 刷新间隔: {}ms, 缓冲阈值: {}, 最大缓冲时间: {}ms",
                FLUSH_INTERVAL_MS, BUFFER_THRESHOLD, MAX_BUFFER_TIME_MS);
    }

    @Override
    public void destroy() {
        // 关闭前刷新所有缓冲区
        flushAllBuffers();
        // 关闭调度器
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("日志缓冲区管理器已关闭");
    }

    /**
     * 生成缓冲区键
     */
    private String generateBufferKey(AppId appId, EndpointId endpointId) {
        return appId.getValue() + ":" + endpointId.getValue();
    }

    /**
     * 日志缓冲区
     */
    @Getter
    private static class LogBuffer {
        private final AppId appId;
        private final EndpointId endpointId;
        private final List<RawLog> logs = new ArrayList<>();
        private final Instant creationTime;

        public LogBuffer(AppId appId, EndpointId endpointId, Instant creationTime) {
            this.appId = appId;
            this.endpointId = endpointId;
            this.creationTime = creationTime;
        }

        public void addLog(RawLog log) {
            logs.add(log);
        }

        public int size() {
            return logs.size();
        }

        public boolean isEmpty() {
            return logs.isEmpty();
        }

    }

}
