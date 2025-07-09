package cn.cug.sxy.domain.reception.model.valobj;

import cn.cug.sxy.types.model.ValueObject;
import lombok.Getter;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @version 1.0
 * @Date 2025/7/7 11:25
 * @Description 原始日志值对象
 * @Author jerryhotton
 */

@Getter
public class RawLog implements ValueObject {

    /**
     * 日志内容
     */
    private final String content;
    /**
     * 来源ID
     */
    private final String sourceId;
    /**
     * 日志格式
     */
    private final LogFormat format;
    /**
     * 时间戳
     */
    private final Instant timestamp;
    /**
     * 元数据
     */
    private final Map<String, String> metadata;

    public RawLog(String content, String sourceId, LogFormat format,
                  Instant timestamp, Map<String, String> metadata) {
        this.content = content;
        this.sourceId = sourceId;
        this.format = format;
        this.timestamp = timestamp;
        this.metadata = Collections.unmodifiableMap(new HashMap<>(metadata));
    }

    /**
     * 创建带有附加元数据的新实例
     */
    public RawLog withAddedMetadata(String key, String value) {
        Map<String, String> newMetadata = new HashMap<>(this.metadata);
        newMetadata.put(key, value);
        return new RawLog(content, sourceId, format, timestamp, newMetadata);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RawLog rawLog = (RawLog) o;
        return Objects.equals(content, rawLog.content) &&
                Objects.equals(sourceId, rawLog.sourceId) &&
                format == rawLog.format &&
                Objects.equals(timestamp, rawLog.timestamp) &&
                Objects.equals(metadata, rawLog.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, sourceId, format, timestamp, metadata);
    }

}
