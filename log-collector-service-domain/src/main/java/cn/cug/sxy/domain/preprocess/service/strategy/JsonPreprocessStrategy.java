package cn.cug.sxy.domain.preprocess.service.strategy;

import cn.cug.sxy.domain.reception.model.valobj.LogFormat;
import cn.cug.sxy.domain.reception.model.valobj.ProcessedLog;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @version 1.0
 * @Date 2025/7/9 13:55
 * @Description JSON格式日志预处理策略
 * @Author jerryhotton
 */

@Slf4j
@Component
public class JsonPreprocessStrategy extends AbstractPreprocessStrategy {

    private final ObjectMapper objectMapper;

    public JsonPreprocessStrategy(ObjectMapper objectMapper) {
        super(LogFormat.JSON);
        this.objectMapper = objectMapper;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void process(ProcessedLog log) {
        try {
            // 解析JSON内容
            JsonNode rootNode = objectMapper.readTree(log.getContent());

            // 提取关键字段到元数据
            Map<String, String> extractedFields = extractFields(rootNode);

            // 将提取的字段添加到元数据中
            for (Map.Entry<String, String> entry : extractedFields.entrySet()) {
                log.addMetadata(entry.getKey(), entry.getValue());
            }

            // 将解析后的JSON添加到元数据中
            // 这里我们可以添加一个标记，表示该日志已经被预处理过
            log.addMetadata("preprocessed", "true");
            log.addMetadata("preprocessedAt", String.valueOf(System.currentTimeMillis()));

            // 添加结构化字段
            try {
                // 将整个JSON添加为结构化字段
                Map<String, Object> jsonMap = objectMapper.convertValue(rootNode, Map.class);
                for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
                    log.addStructuredField(entry.getKey(), entry.getValue());
                }
            } catch (Exception e) {
                log.warn("无法将JSON解析为Map: " + e.getMessage());
            }

        } catch (Exception e) {
            log.markValidationFailed("JSON解析失败: " + e.getMessage());
            log.error("JSON预处理失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public boolean supportsBatchProcessing() {
        // JSON处理可以批量进行
        return true;
    }

    @Override
    public void processBatch(List<ProcessedLog> logs) {
        // 批量处理JSON日志
        // 这里可以实现更高效的批量处理逻辑
        for (ProcessedLog log : logs) {
            process(log);
        }
    }

    /**
     * 从JSON中提取关键字段
     *
     * @param rootNode JSON根节点
     * @return 提取的字段Map
     */
    private Map<String, String> extractFields(JsonNode rootNode) {
        Map<String, String> fields = new HashMap<>();

        // 提取一级字段
        Iterator<Map.Entry<String, JsonNode>> fieldsIterator = rootNode.fields();
        while (fieldsIterator.hasNext()) {
            Map.Entry<String, JsonNode> field = fieldsIterator.next();
            String key = field.getKey();
            JsonNode value = field.getValue();

            // 只提取简单类型的字段
            if (value.isValueNode()) {
                fields.put("json." + key, value.asText());
            }
        }

        // 提取特定的嵌套字段
        extractNestedField(rootNode, "timestamp", fields);
        extractNestedField(rootNode, "level", fields);
        extractNestedField(rootNode, "message", fields);
        extractNestedField(rootNode, "logger", fields);
        extractNestedField(rootNode, "thread", fields);

        return fields;
    }

    /**
     * 提取嵌套字段
     *
     * @param rootNode  JSON根节点
     * @param fieldPath 字段路径，用点分隔
     * @param fields    结果Map
     */
    private void extractNestedField(JsonNode rootNode, String fieldPath, Map<String, String> fields) {
        String[] parts = fieldPath.split("\\.");
        JsonNode currentNode = rootNode;

        for (String part : parts) {
            currentNode = currentNode.get(part);
            if (currentNode == null) {
                return;
            }
        }

        if (currentNode.isValueNode()) {
            fields.put("json." + fieldPath, currentNode.asText());
        }
    }


} 