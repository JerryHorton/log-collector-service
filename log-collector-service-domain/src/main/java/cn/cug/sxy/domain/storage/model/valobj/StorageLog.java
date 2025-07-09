package cn.cug.sxy.domain.storage.model.valobj;

import cn.cug.sxy.types.model.ValueObject;
import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @version 1.0
 * @Date 2025/7/9 14:04
 * @Description 存储日志值对象
 * @Author jerryhotton
 */

@Getter
public class StorageLog implements ValueObject {

    // 日志内容
    private final String content;

    // 元数据
    private final Map<String, String> metadata;

    // 结构化字段
    private final Map<String, Object> structuredFields;

    /**
     * 构造函数
     *
     * @param content 日志内容
     * @param metadata 元数据
     * @param structuredFields 结构化字段
     */
    public StorageLog(String content, Map<String, String> metadata, Map<String, Object> structuredFields) {
        this.content = content;
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        this.structuredFields = structuredFields != null ? new HashMap<>(structuredFields) : new HashMap<>();
    }

    /**
     * 获取不可变元数据视图
     *
     * @return 不可变元数据视图
     */
    public Map<String, String> getMetadataView() {
        return Collections.unmodifiableMap(metadata);
    }

    /**
     * 获取不可变结构化字段视图
     *
     * @return 不可变结构化字段视图
     */
    public Map<String, Object> getStructuredFieldsView() {
        return Collections.unmodifiableMap(structuredFields);
    }

    /**
     * 获取特定元数据
     *
     * @param key 元数据键
     * @return 元数据值，如果不存在则返回null
     */
    public String getMetadata(String key) {
        return metadata.get(key);
    }

    /**
     * 获取特定结构化字段
     *
     * @param key 字段键
     * @return 字段值，如果不存在则返回null
     */
    public Object getStructuredField(String key) {
        return structuredFields.get(key);
    }

}
