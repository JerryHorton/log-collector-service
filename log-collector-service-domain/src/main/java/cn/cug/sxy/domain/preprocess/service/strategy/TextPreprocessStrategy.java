package cn.cug.sxy.domain.preprocess.service.strategy;

import cn.cug.sxy.domain.reception.model.valobj.LogFormat;
import cn.cug.sxy.domain.reception.model.valobj.ProcessedLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @version 1.0
 * @Date 2025/7/17 09:38
 * @Description 文本格式日志预处理策略
 * @Author jerryhotton
 */

@Slf4j
@Component
public class TextPreprocessStrategy extends AbstractPreprocessStrategy {

    // 常见日志格式的正则表达式模式
    private static final List<LogPatternDefinition> LOG_PATTERNS = new ArrayList<>();

    // 初始化常见日志格式的正则表达式
    static {
        // Log4j2格式: YY-MM-DD.HH:MM:SS.SSS [thread-name] LEVEL class-name serviceId -traceId message
        LOG_PATTERNS.add(new LogPatternDefinition(
                "LOG4J2_PATTERN",
                Pattern.compile("^(\\d{2}-\\d{2}-\\d{2}\\.\\d{2}:\\d{2}:\\d{2}\\.\\d{3})\\s+\\[([^]]+)]\\s+(\\w+)\\s+(\\S+)\\s+(?:(\\S*)\\s+-)?(?:(\\S*)\\s+)?(.*)$"),
                Arrays.asList("timestamp", "thread", "level", "logger", "serviceId", "traceId", "message")
        ));
        // 标准日志格式: 2023-07-09 13:55:30,123 [thread-1] INFO com.example.Class - Log message
        LOG_PATTERNS.add(new LogPatternDefinition(
                "STANDARD",
                Pattern.compile("^(\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}[,.]\\d{3})\\s+\\[(.*?)]\\s+(\\w+)\\s+(\\S+)\\s+-\\s+(.*)$"),
                Arrays.asList("timestamp", "thread", "level", "logger", "message")
        ));
        // 简单日志格式: 2023-07-09 13:55:30 INFO Log message
        LOG_PATTERNS.add(new LogPatternDefinition(
                "SIMPLE",
                Pattern.compile("^(\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2})\\s+(\\w+)\\s+(.*)$"),
                Arrays.asList("timestamp", "level", "message")
        ));
        // Apache访问日志格式: 127.0.0.1 - frank [10/Oct/2000:13:55:36 -0700] "GET /apache_pb.gif HTTP/1.0" 200 2326
        LOG_PATTERNS.add(new LogPatternDefinition(
                "APACHE_ACCESS",
                Pattern.compile("^(\\S+) (\\S+) (\\S+) \\[([^]]+)] \"([^\"]*)\" (\\d+) (\\d+)"),
                Arrays.asList("ip", "identity", "user", "timestamp", "request", "status", "size")
        ));
        // Nginx访问日志格式
        LOG_PATTERNS.add(new LogPatternDefinition(
                "NGINX_ACCESS",
                Pattern.compile("^(\\S+) - (\\S+) \\[([^]]+)] \"([^\"]*)\" (\\d+) (\\d+) \"([^\"]*)\" \"([^\"]*)\""),
                Arrays.asList("ip", "user", "timestamp", "request", "status", "size", "referer", "user_agent")
        ));
        // 自定义键值对格式: key1=value1 key2="value with spaces" key3=value3
        LOG_PATTERNS.add(new LogPatternDefinition(
                "KEY_VALUE",
                Pattern.compile("(\\w+)=(?:\"([^\"]*)\"|(\\S*))")
        ));
    }

    public TextPreprocessStrategy() {
        super(LogFormat.TEXT);
    }

    @Override
    public void process(ProcessedLog log) {
        try {
            String content = log.getContent();
            if (content == null || content.trim().isEmpty()) {
                log.markValidationFailed("日志内容为空");
                return;
            }
            // 处理多行日志，分离主日志行和异常堆栈
            Map<String, Object> parsedContent = parseMultilineLog(content);
            String mainLogLine = (String) parsedContent.get("mainLogLine");
            @SuppressWarnings("unchecked")
            List<String> stackTraceLines = (List<String>) parsedContent.get("stackTrace");
            // 尝试识别日志格式并提取字段
            Map<String, String> extractedFields = new HashMap<>();
            String logFormat = identifyAndExtractFields(mainLogLine, extractedFields);
            // 添加格式信息到元数据
            log.addMetadata("text.format", logFormat);
            log.addMetadata("preprocessed", "true");
            log.addMetadata("preprocessedAt", String.valueOf(System.currentTimeMillis()));
            // 将提取的字段添加到元数据和结构化字段
            for (Map.Entry<String, String> entry : extractedFields.entrySet()) {
                String key = "text." + entry.getKey();
                log.addMetadata(key, entry.getValue());
                log.addStructuredField(entry.getKey(), entry.getValue());
            }
            // 处理异常堆栈信息
            if (!stackTraceLines.isEmpty()) {
                processExceptionStackTrace(log, stackTraceLines);
            }
            // 执行内容分析
            analyzeContent(log, content, extractedFields);
            // 提取JSON或键值对数据（如果存在）
            extractStructuredData(log, extractedFields.get("message"));
        } catch (Exception e) {
            log.markValidationFailed("文本预处理失败: " + e.getMessage());
            log.error("文本预处理失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public boolean supportsBatchProcessing() {
        // 文本处理可以批量进行，但每条日志需要单独分析
        return true;
    }

    @Override
    public void processBatch(List<ProcessedLog> logs) {
        // 批量处理文本日志
        logs.parallelStream().forEach(this::process);
    }

    /**
     * 解析多行日志，分离主日志行和异常堆栈
     *
     * @param content 完整日志内容
     * @return 解析结果，包含主日志行和异常堆栈
     */
    private Map<String, Object> parseMultilineLog(String content) {
        Map<String, Object> result = new HashMap<>();
        List<String> lines = Arrays.asList(content.split("\n"));
        // 查找第一个非空行作为主日志行
        String mainLogLine = "";
        int stackTraceStartIndex = -1;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (!line.isEmpty()) {
                // 如果还没找到主日志行，则当前行为主日志行
                if (mainLogLine.isEmpty()) {
                    mainLogLine = line;
                }
                // 检查是否为异常堆栈的开始（以异常类名开头）
                if (line.matches("^[a-zA-Z]+(\\.[a-zA-Z]+)*(Exception|Error|Throwable)(:\\s.*)?$")) {
                    stackTraceStartIndex = i;
                    break;
                }
            }
        }
        result.put("mainLogLine", mainLogLine);
        // 提取异常堆栈（如果存在）
        List<String> stackTrace = new ArrayList<>();
        if (stackTraceStartIndex >= 0) {
            stackTrace = lines.subList(stackTraceStartIndex, lines.size());
        }
        result.put("stackTrace", stackTrace);

        return result;
    }

    /**
     * 识别日志格式并提取字段
     *
     * @param content         日志内容
     * @param extractedFields 提取的字段Map
     * @return 识别到的日志格式名称
     */
    private String identifyAndExtractFields(String content, Map<String, String> extractedFields) {
        // 尝试匹配所有预定义的日志格式
        for (LogPatternDefinition patternDef : LOG_PATTERNS) {
            Matcher matcher = patternDef.getPattern().matcher(content);
            // 对于KEY_VALUE格式，使用特殊处理
            if ("KEY_VALUE".equals(patternDef.getName())) {
                matcher.reset();
                boolean found = false;
                while (matcher.find()) {
                    found = true;
                    String key = matcher.group(1);
                    String value = matcher.group(2) != null ? matcher.group(2) : matcher.group(3);
                    extractedFields.put(key, value);
                }
                if (found) {
                    return "KEY_VALUE";
                }
            } else if (matcher.matches()) {
                // 对于其他格式，使用标准处理
                List<String> fieldNames = patternDef.getFieldNames();
                for (int i = 0; i < fieldNames.size(); i++) {
                    String fieldName = fieldNames.get(i);
                    String fieldValue = matcher.group(i + 1);
                    extractedFields.put(fieldName, fieldValue);
                }
                return patternDef.getName();
            }
        }

        // 如果没有匹配到预定义格式，尝试基于行分析
        return analyzeByLines(content, extractedFields);
    }

    /**
     * 基于行分析日志内容
     *
     * @param content         日志内容
     * @param extractedFields 提取的字段Map
     * @return 分析结果格式名称
     */
    private String analyzeByLines(String content, Map<String, String> extractedFields) {
        String[] lines = content.split("\\n");
        // 提取第一行作为主要消息
        if (lines.length > 0) {
            extractedFields.put("message", lines[0]);
        }
        // 如果有多行，可能包含堆栈跟踪
        if (lines.length > 1) {
            String stackTrace = String.join("\n", Arrays.copyOfRange(lines, 1, lines.length));
            extractedFields.put("stackTrace", stackTrace);
        }

        return "UNSTRUCTURED";
    }

    /**
     * 处理异常堆栈信息
     *
     * @param log             处理中的日志对象
     * @param stackTraceLines 异常堆栈行
     */
    private void processExceptionStackTrace(ProcessedLog log, List<String> stackTraceLines) {
        if (stackTraceLines.isEmpty()) {
            return;
        }
        log.addMetadata("text.containsException", "true");
        log.addStructuredField("containsException", true);
        // 提取异常类型和消息
        String firstLine = stackTraceLines.get(0);
        Pattern exceptionPattern = Pattern.compile("^([a-zA-Z]+(\\.[a-zA-Z]+)*(Exception|Error|Throwable))(:\\s*(.*))?$");
        Matcher matcher = exceptionPattern.matcher(firstLine);

        if (matcher.find()) {
            String exceptionType = matcher.group(1);
            String exceptionMessage = matcher.group(5) != null ? matcher.group(5) : "";
            log.addMetadata("text.exceptionType", exceptionType);
            log.addStructuredField("exceptionType", exceptionType);
            log.addMetadata("text.exceptionMessage", exceptionMessage);
            log.addStructuredField("exceptionMessage", exceptionMessage);
        }
        // 提取堆栈信息
        List<Map<String, String>> stackFrames = new ArrayList<>();
        Pattern stackFramePattern = Pattern.compile("\\s+at\\s+([^(]+)\\(([^:]+)(?::([0-9]+))?\\)");
        for (String line : stackTraceLines) {
            Matcher frameMatcher = stackFramePattern.matcher(line);
            if (frameMatcher.find()) {
                Map<String, String> frame = new HashMap<>();
                frame.put("method", frameMatcher.group(1).trim());
                frame.put("file", frameMatcher.group(2).trim());
                if (frameMatcher.group(3) != null) {
                    frame.put("line", frameMatcher.group(3));
                }
                stackFrames.add(frame);
            }
        }
        // 提取 Caused by 异常
        List<Map<String, String>> causedByExceptions = new ArrayList<>();
        Pattern causedByPattern = Pattern.compile("Caused by:\\s+([a-zA-Z]+(\\.[a-zA-Z]+)*(Exception|Error|Throwable))(:\\s*(.*))?");
        for (String line : stackTraceLines) {
            Matcher causedByMatcher = causedByPattern.matcher(line);
            if (causedByMatcher.find()) {
                Map<String, String> causedException = new HashMap<>();
                causedException.put("type", causedByMatcher.group(1));
                causedException.put("message", causedByMatcher.group(5) != null ? causedByMatcher.group(5) : "");
                causedByExceptions.add(causedException);
            }
        }
        // 将完整堆栈添加到结构化字段
        String fullStackTrace = String.join("\n", stackTraceLines);
        log.addStructuredField("stackTrace", fullStackTrace);
        if (!stackFrames.isEmpty()) {
            log.addStructuredField("stackFrames", stackFrames);
        }
        if (!causedByExceptions.isEmpty()) {
            log.addStructuredField("causedByExceptions", causedByExceptions);
        }
        // 提取根本原因
        if (!causedByExceptions.isEmpty()) {
            Map<String, String> rootCause = causedByExceptions.get(causedByExceptions.size() - 1);
            log.addMetadata("text.rootCauseType", rootCause.get("type"));
            log.addMetadata("text.rootCauseMessage", rootCause.get("message"));
            log.addStructuredField("rootCause", rootCause);
        }
    }

    /**
     * 分析日志内容，提取额外信息
     *
     * @param log             处理中的日志对象
     * @param content         日志内容
     * @param extractedFields 已提取的字段
     */
    private void analyzeContent(ProcessedLog log, String content, Map<String, String> extractedFields) {
        // 提取日志级别（如果尚未提取）
        if (!extractedFields.containsKey("level")) {
            extractLogLevel(content, extractedFields);
        }

        // 提取时间戳（如果尚未提取）
        if (!extractedFields.containsKey("timestamp")) {
            extractTimestamp(content, extractedFields);
        }

        // 计算日志的统计信息
        calculateStatistics(log, content);

        // 检测敏感信息
        detectSensitiveInfo(log, content);
    }

    /**
     * 提取日志级别
     *
     * @param content         日志内容
     * @param extractedFields 提取的字段Map
     */
    private void extractLogLevel(String content, Map<String, String> extractedFields) {
        // 常见日志级别关键字
        String[] levels = {"ERROR", "WARN", "INFO", "DEBUG", "TRACE", "FATAL", "CRITICAL"};

        for (String level : levels) {
            if (content.contains(level)) {
                extractedFields.put("level", level);
                return;
            }
        }
    }

    /**
     * 提取时间戳
     *
     * @param content         日志内容
     * @param extractedFields 提取的字段Map
     */
    private void extractTimestamp(String content, Map<String, String> extractedFields) {
        // 常见的时间戳格式
        List<Pattern> timestampPatterns = Arrays.asList(
                Pattern.compile("\\d{2}-\\d{2}-\\d{2}\\.\\d{2}:\\d{2}:\\d{2}\\.\\d{3}"), // YY-MM-DD.HH:MM:SS.SSS
                Pattern.compile("\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}[,.]\\d{3}"),
                Pattern.compile("\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}"),
                Pattern.compile("\\d{4}/\\d{2}/\\d{2}\\s\\d{2}:\\d{2}:\\d{2}"),
                Pattern.compile("\\d{2}/\\w{3}/\\d{4}:\\d{2}:\\d{2}:\\d{2}")
        );

        for (Pattern pattern : timestampPatterns) {
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                extractedFields.put("timestamp", matcher.group());
                return;
            }
        }
    }

    /**
     * 计算日志的统计信息
     *
     * @param log     处理中的日志对象
     * @param content 日志内容
     */
    private void calculateStatistics(ProcessedLog log, String content) {
        // 计算行数
        int lineCount = content.split("\\n").length;
        log.addMetadata("text.lineCount", String.valueOf(lineCount));

        // 计算单词数
        int wordCount = content.split("\\s+").length;
        log.addMetadata("text.wordCount", String.valueOf(wordCount));

        // 计算字符数
        log.addMetadata("text.charCount", String.valueOf(content.length()));
    }

    /**
     * 检测敏感信息
     *
     * @param log     处理中的日志对象
     * @param content 日志内容
     */
    private void detectSensitiveInfo(ProcessedLog log, String content) {
        // 检测IP地址
        Pattern ipPattern = Pattern.compile("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b");
        Matcher ipMatcher = ipPattern.matcher(content);
        if (ipMatcher.find()) {
            log.addMetadata("text.containsIp", "true");
            log.addStructuredField("containsIp", true);

            // 收集所有IP地址
            Set<String> ipAddresses = new HashSet<>();
            do {
                ipAddresses.add(ipMatcher.group());
            } while (ipMatcher.find());

            if (!ipAddresses.isEmpty()) {
                log.addStructuredField("ipAddresses", ipAddresses);
            }
        }

        // 检测邮箱地址
        Pattern emailPattern = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
        Matcher emailMatcher = emailPattern.matcher(content);
        if (emailMatcher.find()) {
            log.addMetadata("text.containsEmail", "true");
            log.addStructuredField("containsEmail", true);
        }

        // 检测URL
        Pattern urlPattern = Pattern.compile("https?://\\S+");
        Matcher urlMatcher = urlPattern.matcher(content);
        if (urlMatcher.find()) {
            log.addMetadata("text.containsUrl", "true");
            log.addStructuredField("containsUrl", true);

            // 收集所有URL
            Set<String> urls = new HashSet<>();
            do {
                urls.add(urlMatcher.group());
            } while (urlMatcher.find());

            if (!urls.isEmpty()) {
                log.addStructuredField("urls", urls);
            }
        }

        // 检测用户ID
        Pattern userIdPattern = Pattern.compile("user[Ii][Dd][\"']?\\s*[:=]\\s*[\"']?([\\w-]+)[\"']?");
        Matcher userIdMatcher = userIdPattern.matcher(content);
        if (userIdMatcher.find()) {
            log.addMetadata("text.containsUserId", "true");
            log.addStructuredField("containsUserId", true);
            log.addStructuredField("userId", userIdMatcher.group(1));
        }
    }

    /**
     * 提取结构化数据（JSON或键值对）
     *
     * @param log     处理中的日志对象
     * @param message 日志消息
     */
    private void extractStructuredData(ProcessedLog log, String message) {
        if (message == null) {
            return;
        }

        // 尝试提取JSON对象
        Pattern jsonPattern = Pattern.compile("\\{[^{}]*((\\{[^{}]*\\})[^{}]*)*\\}");
        Matcher jsonMatcher = jsonPattern.matcher(message);

        if (jsonMatcher.find()) {
            String jsonStr = jsonMatcher.group();
            log.addMetadata("text.containsJson", "true");
            log.addStructuredField("embeddedJson", jsonStr);

            // 提取JSON中的键值对
            try {
                // 这里可以使用Jackson解析JSON，但为了减少依赖，使用简单的正则表达式
                Pattern keyValuePattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(?:\"([^\"]*)\"|([\\d.]+)|true|false|null|\\{[^{}]*\\}|\\[[^\\[\\]]*\\])");
                Matcher kvMatcher = keyValuePattern.matcher(jsonStr);

                Map<String, String> jsonFields = new HashMap<>();
                while (kvMatcher.find()) {
                    String key = kvMatcher.group(1);
                    String value = kvMatcher.group(2) != null ? kvMatcher.group(2) :
                            (kvMatcher.group(3) != null ? kvMatcher.group(3) : kvMatcher.group());
                    jsonFields.put(key, value);
                }

                if (!jsonFields.isEmpty()) {
                    log.addStructuredField("jsonFields", jsonFields);
                }
            } catch (Exception e) {
                log.warn("无法解析嵌入的JSON: " + e.getMessage());
            }
        }

        // 提取键值对
        Pattern kvPattern = Pattern.compile("(\\w+)=(?:\"([^\"]*)\"|([^\\s,}]+))");
        Matcher kvMatcher = kvPattern.matcher(message);

        Map<String, String> keyValuePairs = new HashMap<>();
        while (kvMatcher.find()) {
            String key = kvMatcher.group(1);
            String value = kvMatcher.group(2) != null ? kvMatcher.group(2) : kvMatcher.group(3);
            keyValuePairs.put(key, value);
        }

        if (!keyValuePairs.isEmpty()) {
            log.addMetadata("text.containsKeyValuePairs", "true");
            log.addStructuredField("keyValuePairs", keyValuePairs);
        }
    }

    /**
     * 日志格式定义类
     */
    private static class LogPatternDefinition {
        private final String name;
        private final Pattern pattern;
        private final List<String> fieldNames;

        public LogPatternDefinition(String name, Pattern pattern, List<String> fieldNames) {
            this.name = name;
            this.pattern = pattern;
            this.fieldNames = fieldNames;
        }

        public LogPatternDefinition(String name, Pattern pattern) {
            this(name, pattern, Collections.emptyList());
        }

        public String getName() {
            return name;
        }

        public Pattern getPattern() {
            return pattern;
        }

        public List<String> getFieldNames() {
            return fieldNames;
        }
    }

}
