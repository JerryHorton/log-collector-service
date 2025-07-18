package cn.cug.sxy.domain.reception.model.aggregate;

import cn.cug.sxy.domain.reception.model.valobj.EndpointId;
import cn.cug.sxy.domain.reception.model.valobj.EndpointStatus;
import cn.cug.sxy.domain.reception.model.valobj.EndpointType;
import cn.cug.sxy.domain.reception.model.valobj.LogFormat;
import cn.cug.sxy.types.exception.AppException;
import cn.cug.sxy.types.model.AggregateRoot;
import lombok.Getter;

import java.util.*;

/**
 * @version 1.0
 * @Date 2025/7/7 14:44
 * @Description 接收端点聚合根（表示一个日志接收端点，如HTTP API、SDK接入点或Agent连接点）
 * @Author jerryhotton
 */

@Getter
public class ReceiverEndpoint implements AggregateRoot<EndpointId> {

    /**
     * 端点ID
     */
    private final EndpointId id;
    /**
     * 端点名称
     */
    private String name;
    /**
     * 端点类型
     */
    private EndpointType type;
    /**
     * 协议类型
     */
    private String protocol;
    /**
     * 接收路径
     */
    private String path;
    /**
     * 端口号
     */
    private Integer port;
    /**
     * 日志格式
     */
    private LogFormat format;
    /**
     * 最大负载大小（字节）
     */
    private int maxPayloadSize;
    /**
     * 是否启用压缩
     */
    private boolean compressionEnabled;
    /**
     * 压缩算法
     */
    private String compressionAlgorithm;
    /**
     * 端点状态
     */
    private EndpointStatus status;
    /**
     * 允许访问的应用ID集合
     */
    private Set<String> allowedAppIds;
    /**
     * 最大批次数量
     */
    private int maxBatchCount;
    /**
     * 预处理策略类型
     */
    private String preprocessStrategy;
    /**
     * 是否启用缓冲
     */
    private boolean bufferingEnabled;
    /**
     * 缓冲时间（毫秒）
     */
    private int bufferingTimeMs;
    /**
     * 缓冲大小
     */
    private int bufferingSize;

    public ReceiverEndpoint(EndpointId id, String name, EndpointType type,
                            String protocol, String path, Integer port,
                            LogFormat format, int maxPayloadSize) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.protocol = protocol;
        this.path = path;
        this.port = port;
        this.format = format;
        this.maxPayloadSize = maxPayloadSize;
        this.compressionEnabled = false;
        this.status = EndpointStatus.INACTIVE;
        this.allowedAppIds = new HashSet<>();
        this.maxBatchCount = 1000; // 默认最大批次数量
        this.bufferingEnabled = false;
        this.bufferingTimeMs = 1000; // 默认1秒
        this.bufferingSize = 100; // 默认100条日志

        validateEndpoint();
    }

    private void validateEndpoint() {
        if (maxPayloadSize <= 0 || maxPayloadSize > 100 * 1024 * 1024) {
            throw new AppException("Invalid max payload size: " + maxPayloadSize);
        }

        if (EndpointType.HTTP.equals(type) && (path == null || path.isEmpty())) {
            throw new AppException("HTTP endpoint must have a path");
        }

        if ((EndpointType.TCP.equals(type) || EndpointType.UDP.equals(type)) && port == null) {
            throw new AppException("TCP/UDP endpoint must have a port");
        }
    }

    /**
     * 激活端点
     */
    public void activate() {
        this.status = EndpointStatus.ACTIVE;
    }

    /**
     * 停用端点
     */
    public void deactivate() {
        this.status = EndpointStatus.INACTIVE;
    }

    /**
     * 启用压缩
     */
    public void enableCompression(String algorithm) {
        this.compressionEnabled = true;
        this.compressionAlgorithm = algorithm;
    }

    /**
     * 禁用压缩
     */
    public void disableCompression() {
        this.compressionEnabled = false;
        this.compressionAlgorithm = null;
    }

    /**
     * 允许应用访问
     */
    public void allowApp(String appId) {
        this.allowedAppIds.add(appId);
    }

    /**
     * 禁止应用访问
     */
    public void disallowApp(String appId) {
        this.allowedAppIds.remove(appId);
    }

    /**
     * 检查应用是否允许访问
     */
    public boolean isAppAllowed(String appId) {
        return allowedAppIds.isEmpty() || allowedAppIds.contains(appId);
    }

    /**
     * 更新端点配置
     */
    public void updateConfig(String name, String path, Integer port,
                             LogFormat format, int maxPayloadSize) {
        this.name = name;
        this.path = path;
        this.port = port;
        this.format = format;
        this.maxPayloadSize = maxPayloadSize;

        validateEndpoint();
    }

    /**
     * 设置最大批次数量
     */
    public void setMaxBatchCount(int maxBatchCount) {
        if (maxBatchCount <= 0) {
            throw new AppException("Max batch count must be greater than 0");
        }
        this.maxBatchCount = maxBatchCount;
    }

    /**
     * 设置预处理策略
     */
    public void setPreprocessStrategy(String preprocessStrategy) {
        this.preprocessStrategy = preprocessStrategy;
    }

    /**
     * 启用缓冲
     */
    public void enableBuffering(int bufferingTimeMs, int bufferingSize) {
        if (bufferingTimeMs <= 0) {
            throw new AppException("Buffering time must be greater than 0");
        }
        if (bufferingSize <= 0) {
            throw new AppException("Buffering size must be greater than 0");
        }
        this.bufferingEnabled = true;
        this.bufferingTimeMs = bufferingTimeMs;
        this.bufferingSize = bufferingSize;
    }

    /**
     * 禁用缓冲
     */
    public void disableBuffering() {
        this.bufferingEnabled = false;
    }

    public EndpointId getEndpointId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReceiverEndpoint that = (ReceiverEndpoint) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
