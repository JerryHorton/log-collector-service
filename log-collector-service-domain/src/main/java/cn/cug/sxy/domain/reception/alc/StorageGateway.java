package cn.cug.sxy.domain.reception.alc;

import cn.cug.sxy.domain.reception.model.entity.LogBatchEntity;
import cn.cug.sxy.domain.reception.model.valobj.BatchId;
import cn.cug.sxy.domain.reception.model.valobj.LogFormat;
import cn.cug.sxy.domain.reception.model.valobj.ProcessedLog;
import cn.cug.sxy.domain.storage.model.valobj.StorageLog;
import cn.cug.sxy.domain.storage.service.ILogStorageService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @version 1.0
 * @Date 2025/7/9 14:00
 * @Description 日志存储领域防腐层实现
 * @Author jerryhotton
 */

@Slf4j
@Component
public class StorageGateway implements IStorageGateway {

    private final ILogStorageService logStorageService;

    public StorageGateway(ILogStorageService logStorageService) {
        this.logStorageService = logStorageService;
    }

    @Override
    public BatchStorageResult storeBatch(LogBatchEntity batch, List<ProcessedLog> processedLogs) {
        try {
            // 1. 将处理后的日志转换为存储日志
            List<StorageLog> storageLogs = convertToStorageLogs(batch, processedLogs);
            // 2. 调用存储服务存储日志
            String traceId = logStorageService.storeBatch(batch, storageLogs);
            // 3. 检查存储结果
            if (null != traceId) {
                // 存储成功
                return BatchStorageResult.success(traceId);
            } else {
                // 存储失败
                return BatchStorageResult.failure("存储日志失败");
            }
        } catch (Exception e) {
            log.error("存储日志批次异常: batchId={}, error={}",
                    batch.getId().getValue(), e.getMessage(), e);
            return BatchStorageResult.failure("存储异常: " + e.getMessage());
        }
    }

    @Override
    public CompletableFuture<BatchStorageResult> storeBatchAsync(LogBatchEntity batch, List<ProcessedLog> processedLogs) {
        return null;
    }

    @Override
    public boolean isBatchStored(BatchId batchId) {
        return false;
    }

    @Override
    public BatchStorageStatus queryBatchStatus(BatchId batchId) {
        return null;
    }

    private List<StorageLog> convertToStorageLogs(LogBatchEntity batch, List<ProcessedLog> processedLogs) {
        List<StorageLog> storageLogs = new ArrayList<>(processedLogs.size());

        for (ProcessedLog processedLog : processedLogs) {
            // 1. 准备元数据
            Map<String, String> metadata = new HashMap<>();
            // 复制原始元数据
            if (processedLog.getMetadata() != null) {
                metadata.putAll(processedLog.getMetadata());
                // 基本元数据
                metadata.put("appId", batch.getAppId());
                metadata.put("endpointId", batch.getEndpointId());
                metadata.put("batchId", batch.getId().getValue());
                metadata.put("format", processedLog.getFormat().name());
                metadata.put("sourceId", processedLog.getSourceId());
                metadata.put("timestamp", String.valueOf(processedLog.getTimestamp().toEpochMilli()));
                // 其他常用字段
                metadata.put("source", processedLog.getMetadata().getOrDefault("source", "unknown"));
                metadata.put("hostname", processedLog.getMetadata().getOrDefault("hostname", "unknown"));
                metadata.put("level", processedLog.getMetadata().getOrDefault("level", "INFO"));
            }
            // 2. 准备结构化字段
            // 添加处理信息
            processedLog.addStructuredField("_processed", Boolean.TRUE);
            processedLog.addStructuredField("_process_time", processedLog.getProcessTime().toString());
            // 3. 创建存储日志对象
            StorageLog storageLog = new StorageLog(
                    processedLog.getContent(),                  // 日志内容
                    metadata,                                   // 元数据
                    processedLog.getStructuredFields()          // 结构化字段
            );
            storageLogs.add(storageLog);
        }

        return storageLogs;
    }

}
