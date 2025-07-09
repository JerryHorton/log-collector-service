package cn.cug.sxy.domain.reception.model.valobj;

import cn.cug.sxy.types.model.ValueObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @version 1.0
 * @Date 2025/7/9 10:30
 * @Description 已处理日志值对象（表示经过预处理和验证的日志）
 * @Author jerryhotton
 */

@Slf4j
@Getter
public class ProcessedLog implements ValueObject {

    // 日志内容
    private final String content;

    // 日志格式
    private final LogFormat format;

    // 元数据
    private Map<String, String> metadata;

    // 所属应用ID
    private final AppId appId;

    // 接收端点ID
    private final EndpointId endpointId;

    // 处理时间
    private final Instant processTime;

    // 是否通过验证
    private boolean validated;

    // 验证失败原因
    private String validationFailReason;

    // 提取的结构化字段
    private Map<String, Object> structuredFields;

    /**
     * 构造函数
     *
     * @param content     日志内容
     * @param format      日志格式
     * @param metadata    元数据
     * @param appId       应用ID
     * @param endpointId  端点ID
     * @param processTime 处理时间
     */
    public ProcessedLog(String content, LogFormat format, Map<String, String> metadata,
                        AppId appId, EndpointId endpointId, Instant processTime) {
        this.content = content;
        this.format = format;
        this.metadata = new HashMap<>(metadata);
        this.appId = appId;
        this.endpointId = endpointId;
        this.processTime = processTime;
        this.validated = true; // 默认为已验证通过
        this.structuredFields = new HashMap<>();
    }

    /**
     * 标记验证失败
     *
     * @param reason 失败原因
     */
    public void markValidationFailed(String reason) {
        this.validated = false;
        this.validationFailReason = reason;
    }

    /**
     * 获取客户端IP
     *
     * @return 客户端IP
     */
    public String getClientIp() {
        return metadata.getOrDefault("clientIp", "");
    }

    /**
     * 获取日志大小（字节）
     *
     * @return 日志大小
     */
    public int getSize() {
        return content != null ? content.getBytes().length : 0;
    }

    /**
     * 更新元数据
     *
     * @param newMetadata 新的元数据
     */
    public void updateMetadata(Map<String, String> newMetadata) {
        this.metadata = new HashMap<>(newMetadata);
    }

    /**
     * 添加元数据
     *
     * @param key   键
     * @param value 值
     */
    public void addMetadata(String key, String value) {
        this.metadata.put(key, value);
    }

    /**
     * 添加结构化字段
     *
     * @param key   键
     * @param value 值
     */
    public void addStructuredField(String key, Object value) {
        this.structuredFields.put(key, value);
    }

    /**
     * 获取结构化字段
     *
     * @return 结构化字段Map
     */
    public Map<String, Object> getStructuredFields() {
        return Collections.unmodifiableMap(structuredFields);
    }

    /**
     * 获取元数据的不可变视图
     *
     * @return 元数据的不可变视图
     */
    public Map<String, String> getMetadataView() {
        return Collections.unmodifiableMap(metadata);
    }

    /**
     * 记录警告日志
     *
     * @param message 警告消息
     */
    public void warn(String message) {
        log.warn("[ProcessedLog] {}: {}", getLogIdentifier(), message);
    }

    /**
     * 记录错误日志
     *
     * @param message 错误消息
     * @param args    参数
     */
    public void error(String message, Object... args) {
        log.error("[ProcessedLog] {}:{} {}", getLogIdentifier(), message, args);
    }

    /**
     * 获取日志标识符
     *
     * @return 日志标识符
     */
    private String getLogIdentifier() {
        return "appId=" + appId.getValue() + ", endpointId=" + endpointId.getValue();
    }

}
