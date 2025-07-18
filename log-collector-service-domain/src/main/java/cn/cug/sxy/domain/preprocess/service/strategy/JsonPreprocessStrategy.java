package cn.cug.sxy.domain.preprocess.service.strategy;

import cn.cug.sxy.domain.reception.model.valobj.LogFormat;
import cn.cug.sxy.domain.reception.model.valobj.ProcessedLog;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

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
    private final List<String> priorityFields; // 重要字段列表
    private final Pattern timestampPattern; // 用于识别时间戳字段的正则表达式

    public JsonPreprocessStrategy(ObjectMapper objectMapper) {
        super(LogFormat.JSON);
        this.objectMapper = objectMapper;
        // 初始化重要字段列表
        this.priorityFields = new ArrayList<>();
        this.priorityFields.add("timestamp");
        this.priorityFields.add("time");
        this.priorityFields.add("date");
        this.priorityFields.add("level");
        this.priorityFields.add("severity");
        this.priorityFields.add("message");
        this.priorityFields.add("msg");
        this.priorityFields.add("logger");
        this.priorityFields.add("thread");
        this.priorityFields.add("className");
        this.priorityFields.add("methodName");
        this.priorityFields.add("lineNumber");
        this.priorityFields.add("exception");
        this.priorityFields.add("error");
        // 用于识别时间戳字段的正则表达式
        this.timestampPattern = Pattern.compile("(?i)(timestamp|time|date|dt|created|created_at|createdAt)");
    }

    @Override
    public void process(ProcessedLog log) {
        try {
            long startTime = System.currentTimeMillis();
            // 解析JSON内容
            JsonNode rootNode = objectMapper.readTree(log.getContent());
            // 提取关键字段到元数据
            Map<String, String> extractedFields = extractFields(rootNode);
            // 将提取的字段添加到元数据中
            for (Map.Entry<String, String> entry : extractedFields.entrySet()) {
                log.addMetadata(entry.getKey(), entry.getValue());
            }
            // 智能提取和标准化重要字段
            extractAndStandardizeImportantFields(rootNode, log);
            // 添加结构化字段
            try {
                // 将整个JSON添加为结构化字段
                @SuppressWarnings("unchecked")
                Map<String, Object> jsonMap = objectMapper.convertValue(rootNode, Map.class);
                flattenJsonMap(jsonMap, "", log);
                // 添加预处理元信息
                log.addMetadata("preprocessed", "true");
                log.addMetadata("preprocessedAt", String.valueOf(System.currentTimeMillis()));
                log.addMetadata("preprocessDuration", String.valueOf(System.currentTimeMillis() - startTime));
            } catch (Exception e) {
                log.warn("无法将JSON解析为Map: " + e.getMessage());
                // 在失败的情况下，尝试更简单的处理方式
                fallbackProcessing(rootNode, log);
            }
        } catch (Exception e) {
            log.markValidationFailed("JSON解析失败: " + e.getMessage());
            log.error("JSON预处理失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 平展JSON Map并添加到结构化字段中
     * 处理嵌套对象，将它们展平为点分隔的键
     */
    private void flattenJsonMap(Map<String, Object> jsonMap, String prefix, ProcessedLog log) {
        for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                flattenJsonMap(nestedMap, key, log);
            } else if (value instanceof List) {
                // 对于列表，我们可以将其转换为JSON字符串或者单独处理每个元素
                try {
                    String jsonArray = objectMapper.writeValueAsString(value);
                    log.addStructuredField(key, jsonArray);
                } catch (Exception e) {
                    log.warn("无法序列化数组字段 " + key + ": " + e.getMessage());
                    log.addStructuredField(key, value.toString());
                }
            } else {
                // 对于基本类型的值，直接添加
                log.addStructuredField(key, value);
            }
        }
    }

    /**
     * 智能提取和标准化重要字段
     */
    private void extractAndStandardizeImportantFields(JsonNode rootNode, ProcessedLog log) {
        // 查找并标准化日志级别
        String level = findLogLevel(rootNode);
        if (level != null) {
            log.addMetadata("level", standardizeLogLevel(level));
        }
        // 查找并标准化时间戳
        String timestamp = findTimestamp(rootNode);
        if (timestamp != null) {
            log.addMetadata("timestamp", standardizeTimestamp(timestamp));
        }
        // 提取异常信息
        extractExceptionInfo(rootNode, log);
    }

    /**
     * 查找日志级别字段
     */
    private String findLogLevel(JsonNode rootNode) {
        // 常见的日志级别字段名
        String[] levelFieldNames = {"level", "severity", "loglevel"};
        for (String fieldName : levelFieldNames) {
            JsonNode levelNode = rootNode.get(fieldName);
            if (levelNode != null && levelNode.isTextual()) {
                return levelNode.asText();
            }
        }
        // 递归搜索嵌套字段
        Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            if (field.getValue().isObject()) {
                String level = findLogLevel(field.getValue());
                if (level != null) {
                    return level;
                }
            }
        }

        return null;
    }

    /**
     * 标准化日志级别
     */
    private String standardizeLogLevel(String level) {
        level = level.toUpperCase();
        // 标准化不同格式的日志级别
        if (level.contains("WARN")) return "WARN";
        if (level.contains("ERR")) return "ERROR";
        if (level.contains("INFO")) return "INFO";
        if (level.contains("DEBUG")) return "DEBUG";
        if (level.contains("TRACE")) return "TRACE";
        if (level.contains("FATAL")) return "FATAL";

        return level;
    }

    /**
     * 查找时间戳字段
     */
    private String findTimestamp(JsonNode rootNode) {
        Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String fieldName = field.getKey();
            JsonNode value = field.getValue();
            // 检查字段名是否匹配时间戳模式
            if (timestampPattern.matcher(fieldName).matches()) {
                if (value.isTextual() || value.isNumber()) {
                    return value.asText();
                }
            }
            // 递归检查嵌套对象
            if (value.isObject()) {
                String timestamp = findTimestamp(value);
                if (timestamp != null) {
                    return timestamp;
                }
            }
        }

        return null;
    }

    /**
     * 标准化时间戳格式
     */
    private String standardizeTimestamp(String timestamp) {
        // 如果是13位纯数字，毫秒时间戳
        if (timestamp.matches("\\d{13}")) {
            long ms = Long.parseLong(timestamp);
            Instant instant = Instant.ofEpochMilli(ms);
            return DateTimeFormatter.ISO_INSTANT.format(instant);
        }
        // 如果是10位纯数字，秒级时间戳
        if (timestamp.matches("\\d{10}")) {
            long s = Long.parseLong(timestamp);
            Instant instant = Instant.ofEpochSecond(s);
            return DateTimeFormatter.ISO_INSTANT.format(instant);
        }

        return timestamp;
    }

    /**
     * 提取异常信息
     */
    private void extractExceptionInfo(JsonNode rootNode, ProcessedLog log) {
        // 常见的异常字段名
        String[] exceptionFieldNames = {"exception", "error", "throwable", "stacktrace"};
        for (String fieldName : exceptionFieldNames) {
            JsonNode exNode = rootNode.get(fieldName);
            if (exNode != null) {
                if (exNode.isTextual()) {
                    log.addStructuredField("exception", exNode.asText());
                    return;
                } else if (exNode.isObject()) {
                    try {
                        log.addStructuredField("exception", objectMapper.writeValueAsString(exNode));
                        return;
                    } catch (Exception e) {
                        log.warn("无法序列化异常对象: " + e.getMessage());
                    }
                }
            }
        }
        // 检查message字段中是否包含异常信息
        JsonNode msgNode = rootNode.get("message");
        if (msgNode != null && msgNode.isTextual()) {
            String message = msgNode.asText();
            if (message.contains("Exception") || message.contains("Error:")) {
                log.addStructuredField("possibleException", message);
            }
        }
    }

    /**
     * 从JSON中提取关键字段
     */
    private Map<String, String> extractFields(JsonNode rootNode) {
        Map<String, String> fields = new HashMap<>();

        // 提取一级字段
        Iterator<Map.Entry<String, JsonNode>> fieldsIterator = rootNode.fields();
        while (fieldsIterator.hasNext()) {
            Map.Entry<String, JsonNode> field = fieldsIterator.next();
            String key = field.getKey();
            JsonNode value = field.getValue();

            // 优先提取重要字段
            if (priorityFields.contains(key.toLowerCase()) && value.isValueNode()) {
                fields.put(key, value.asText());
                // 添加带前缀的版本
                fields.put("json." + key, value.asText());
            }
            // 对于其他简单类型字段，只添加带前缀的版本
            else if (value.isValueNode()) {
                fields.put("json." + key, value.asText());
            }
        }

        // 递归提取嵌套字段中的重要信息
        for (String priorityField : priorityFields) {
            extractNestedFieldRecursive(rootNode, priorityField, "", fields);
        }

        return fields;
    }

    /**
     * 递归提取嵌套字段
     */
    private void extractNestedFieldRecursive(JsonNode node, String targetField, String path, Map<String, String> fields) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fieldsIterator = node.fields();
            while (fieldsIterator.hasNext()) {
                Map.Entry<String, JsonNode> field = fieldsIterator.next();
                String key = field.getKey();
                JsonNode value = field.getValue();

                String currentPath = path.isEmpty() ? key : path + "." + key;

                // 如果当前字段名匹配目标字段
                if (key.equalsIgnoreCase(targetField) && value.isValueNode()) {
                    fields.put("json." + currentPath, value.asText());
                    // 如果是根路径下的重要字段，也添加不带前缀的版本
                    if (path.isEmpty() && priorityFields.contains(key.toLowerCase())) {
                        fields.put(key, value.asText());
                    }
                }

                // 递归处理嵌套对象
                if (value.isObject()) {
                    extractNestedFieldRecursive(value, targetField, currentPath, fields);
                }
            }
        }
    }

    /**
     * 当标准处理失败时的后备处理方法
     */
    private void fallbackProcessing(JsonNode rootNode, ProcessedLog log) {
        log.warn("使用后备方法处理JSON");

        // 使用简单的字符串表示
        log.addStructuredField("rawJson", rootNode.toString());

        // 尝试提取基本字段
        if (rootNode.has("message")) {
            log.addStructuredField("message", rootNode.get("message").asText());
        }

        if (rootNode.has("level")) {
            log.addStructuredField("level", rootNode.get("level").asText());
        }
    }

    @Override
    public boolean supportsBatchProcessing() {
        return true;
    }

    @Override
    public void processBatch(List<ProcessedLog> logs) {
        // 这里可以实现更高效的批量处理逻辑，例如使用线程池并行处理
        logs.parallelStream().forEach(this::process);
    }

} 