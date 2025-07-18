package cn.cug.sxy.infrastructure.elastic;

import cn.cug.sxy.domain.reception.model.valobj.BatchId;
import cn.cug.sxy.domain.reception.model.valobj.BatchStatus;
import cn.cug.sxy.domain.reception.model.valobj.RawLog;
import cn.cug.sxy.domain.storage.model.entity.LogDocument;
import cn.cug.sxy.domain.storage.model.valobj.StorageLog;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @version 1.0
 * @Date 2025/7/11 15:22
 * @Description
 * @Author jerryhotton
 */

@Slf4j
@Service
public class ElasticsearchLogStorageService implements IElasticsearchLogStorageService {

    private final ElasticsearchClient elasticsearchClient;

    @Value("${elasticsearch.index-prefix:logs}")
    private String indexPrefix;

    public ElasticsearchLogStorageService(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    @Override
    public String storeBatch(BatchId batchId, List<StorageLog> logs) {
        try {
            // 获取当前日期的索引名，按日期分片
            String indexName = getIndexName();
            // 创建批量请求构建器
            BulkRequest.Builder bulkBuilder = new BulkRequest.Builder();
            // 将每条日志添加到批量请求中
            for (StorageLog log : logs) {
                // 转换为ES文档模型
                LogDocument document = convertToDocument(batchId, log);
                // 添加到批量索引操作中
                bulkBuilder.operations(op -> op
                        .index(idx -> idx
                                .index(indexName)
                                .id(document.getId())
                                .document(document)
                        )
                );
            }
            // 执行批量请求
            BulkResponse response = elasticsearchClient.bulk(bulkBuilder.build());
            // 检查批量操作结果
            if (response.errors()) {
                log.error("批量存储日志到ES时发生错误: batchId={}", batchId.getValue());
                // 记录每个失败项的详细信息，便于排查
                for (BulkResponseItem item : response.items()) {
                    if (item.error() != null) {
                        log.error("文档错误: {}", item.error().reason());
                    }
                }
                return null;
            }
            // 返回批次ID作为跟踪ID
            return batchId.getValue();
        } catch (Exception e) {
            // 捕获所有异常，确保上层调用不会因存储问题而中断
            log.error("存储日志批次到ES异常: batchId={}, error={}",
                    batchId.getValue(), e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean isBatchStored(BatchId batchId) {
        try {
            // 执行计数查询，检查包含指定批次ID的文档数量
            var response = elasticsearchClient.count(c -> c
                    .index(getIndexName())
                    .query(q -> q
                            .term(t -> t
                                    .field("batchId")
                                    .value(batchId.getValue())
                            )
                    )
            );
            // 如果文档数大于0，则说明批次已存储
            return response.count() > 0;
        } catch (Exception e) {
            log.error("检查批次是否已存储异常: batchId={}, error={}",
                    batchId.getValue(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public BatchStatus queryBatchStatus(BatchId batchId) {
        // 如果批次已存储，返回已处理状态，否则返回失败状态
        return isBatchStored(batchId) ? BatchStatus.PROCESSED : BatchStatus.FAILED;
    }

    @Override
    public boolean storeLog(StorageLog storageLog) {
        try {
            // 获取当前索引名
            String indexName = getIndexName();
            // 为单条日志生成新的批次ID和文档
            LogDocument document = convertToDocument(BatchId.generate(), storageLog);
            // 执行索引操作
            var response = elasticsearchClient.index(i -> i
                    .index(indexName)
                    .id(document.getId())
                    .document(document)
            );
            // 检查索引结果
            return !response.result().name().equals("Error");
        } catch (Exception e) {
            log.error("存储单条日志到ES异常: error={}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<RawLog> loadLogsForBatch(String batchId, String appId, String endpointId) {
        return List.of();
    }

    /**
     * 获取当前日期的索引名称
     * 按日期分片索引，提高查询效率并便于管理
     *
     * @return 格式化的索引名称，如 logs-2025.07.13
     */
    private String getIndexName() {
        // 按日期分索引，例如 logs-2025.07.13
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        return indexPrefix + "-" + LocalDate.now().format(formatter);
    }

    /**
     * 将存储日志转换为Elasticsearch文档
     * 提取元数据，构建完整的文档模型
     *
     * @param batchId    批次ID
     * @param storageLog 存储日志对象
     * @return 转换后的ES文档对象
     */
    private LogDocument convertToDocument(BatchId batchId, StorageLog storageLog) {
        LogDocument document = new LogDocument();
        // 生成唯一文档ID
        document.setId(UUID.randomUUID().toString());
        document.setBatchId(batchId.getValue());
        // 从元数据中提取字段信息
        Map<String, String> metadata = storageLog.getMetadata();
        // 设置核心字段
        document.setAppId(metadata.getOrDefault("appId", "unknown"));
        document.setEndpointId(metadata.getOrDefault("endpointId", "unknown"));
        document.setContent(storageLog.getContent());
        document.setFormat(metadata.getOrDefault("format", "unknown"));
        document.setSourceId(metadata.getOrDefault("sourceId", "unknown"));
        // 解析时间戳，默认当前时间
        try {
            document.setTimestamp(Long.parseLong(metadata.getOrDefault("timestamp", "0")));
        } catch (NumberFormatException e) {
            document.setTimestamp(System.currentTimeMillis());
        }
        // 解析标签字符串为Map结构
        String tagsStr = metadata.getOrDefault("tags", "");
        Map<String, String> tags = new HashMap<>();
        if (!tagsStr.isEmpty()) {
            // 标签格式: key1=value1,key2=value2
            String[] tagPairs = tagsStr.split(",");
            for (String pair : tagPairs) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    tags.put(kv[0], kv[1]);
                }
            }
        }
        document.setTags(tags);
        // 设置其他元数据字段
        document.setSource(metadata.getOrDefault("source", "unknown"));
        document.setHostname(metadata.getOrDefault("hostname", "unknown"));
        document.setLevel(metadata.getOrDefault("level", "INFO"));
        // 设置结构化数据
        document.setStructuredData(storageLog.getStructuredFields());
        // 记录索引时间，用于区分生成时间和处理时间
        document.setIndexTime(Instant.now());
        // 将结构化数据值合并为一个可搜索的文本
        if (storageLog.getStructuredFields() != null && !storageLog.getStructuredFields().isEmpty()) {
            StringBuilder allValues = new StringBuilder();
            for (Map.Entry<String, Object> entry : storageLog.getStructuredFields().entrySet()) {
                if (entry.getValue() != null) {
                    allValues.append(entry.getValue()).append(" ");
                }
            }
            document.setStructuredDataText(allValues.toString());
        }

        return document;
    }

}
