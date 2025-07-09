package cn.cug.sxy.domain.reception.model.aggregate;

import cn.cug.sxy.domain.reception.model.valobj.AccessKey;
import cn.cug.sxy.domain.reception.model.valobj.AppAccessStatus;
import cn.cug.sxy.domain.reception.model.valobj.AppId;
import cn.cug.sxy.domain.reception.model.valobj.EndpointId;
import cn.cug.sxy.types.exception.AppException;
import cn.cug.sxy.types.model.AggregateRoot;
import lombok.Getter;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @version 1.0
 * @Date 2025/7/7 15:22
 * @Description 应用接入聚合根（表示一个接入日志系统的应用及其访问凭证）
 * @Author jerryhotton
 */

@Getter
public class AppAccess implements AggregateRoot<AppId> {

    /**
     * 应用ID
     */
    private final AppId appId;
    /**
     * 应用名称
     */
    private String appName;
    /**
     * 访问密钥
     */
    private AccessKey accessKey;
    /**
     * 密钥
     */
    private String secretKey;
    /**
     * 允许访问的端点
     */
    private Set<EndpointId> allowedEndpoints;
    /**
     * IP白名单
     */
    private Set<String> ipWhitelist;
    /**
     * 速率限制(次/分钟)
     */
    private int rateLimit;
    /**
     * 突发容量
     */
    private int burstCapacity;
    /**
     * 状态
     */
    private AppAccessStatus status;
    /**
     * 过期时间
     */
    private Instant expiryTime;
    /**
     * 创建时间
     */
    private final Instant createdTime;
    /**
     * 最后访问时间
     */
    private Instant lastAccessTime;

    // IP地址正则表达式
    private static final Pattern IP_PATTERN =
            Pattern.compile("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");


    public AppAccess(AppId id, String appName, AccessKey accessKey, String secretKey, int rateLimit) {
        this.appId = id;
        this.appName = appName;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.rateLimit = rateLimit;
        this.burstCapacity = rateLimit * 2; // 默认突发容量为速率限制的2倍
        this.status = AppAccessStatus.INACTIVE;
        this.allowedEndpoints = new HashSet<>();
        this.ipWhitelist = new HashSet<>();
        this.createdTime = Instant.now();

        validateRateLimit(rateLimit);
    }

    /**
     * 完整构造函数，包含突发容量
     */
    public AppAccess(AppId id, String appName, AccessKey accessKey, String secretKey,
                     int rateLimit, int burstCapacity) {
        this.appId = id;
        this.appName = appName;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.rateLimit = rateLimit;
        this.burstCapacity = burstCapacity;
        this.status = AppAccessStatus.INACTIVE;
        this.allowedEndpoints = new HashSet<>();
        this.ipWhitelist = new HashSet<>();
        this.createdTime = Instant.now();

        validateRateLimit(rateLimit);
        validateBurstCapacity(burstCapacity);
    }

    /**
     * 激活应用接入
     */
    public void activate() {
        if (status == AppAccessStatus.BLOCKED) {
            throw new AppException("无法激活已被阻止的应用");
        }
        this.status = AppAccessStatus.ACTIVE;
    }

    /**
     * 停用应用接入
     */
    public void deactivate() {
        if (status == AppAccessStatus.BLOCKED) {
            throw new AppException("无法停用已被阻止的应用");
        }
        this.status = AppAccessStatus.INACTIVE;
    }

    /**
     * 阻止应用接入
     */
    public void block() {
        this.status = AppAccessStatus.BLOCKED;
    }

    /**
     * 更新访问密钥
     */
    public void updateAccessKey(AccessKey accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    /**
     * 设置过期时间
     */
    public void setExpiryTime(Instant expiryTime) {
        if (expiryTime != null && expiryTime.isBefore(Instant.now())) {
            throw new AppException("过期时间不能早于当前时间");
        }
        this.expiryTime = expiryTime;
    }

    /**
     * 添加允许访问的端点
     */
    public void allowEndpoint(EndpointId endpointId) {
        this.allowedEndpoints.add(endpointId);
    }

    /**
     * 移除允许访问的端点
     */
    public void disallowEndpoint(EndpointId endpointId) {
        this.allowedEndpoints.remove(endpointId);
    }

    /**
     * 检查是否允许访问端点
     */
    public boolean isEndpointAllowed(EndpointId endpointId) {
        return allowedEndpoints.isEmpty() || allowedEndpoints.contains(endpointId);
    }

    /**
     * 添加IP白名单
     */
    public void addIpToWhitelist(String ip) {
        validateIp(ip);
        this.ipWhitelist.add(ip);
    }

    /**
     * 从白名单移除IP
     */
    public void removeIpFromWhitelist(String ip) {
        this.ipWhitelist.remove(ip);
    }

    /**
     * 检查IP是否在白名单中
     */
    public boolean isIpWhitelisted(String ip) {
        // 如果白名单为空，则允许所有IP
        return ipWhitelist.isEmpty() || ipWhitelist.contains(ip);
    }

    /**
     * 更新速率限制
     */
    public void updateRateLimit(int rateLimit) {
        validateRateLimit(rateLimit);
        this.rateLimit = rateLimit;
        // 默认更新突发容量为速率限制的2倍
        this.burstCapacity = rateLimit * 2;
    }

    /**
     * 更新速率限制和突发容量
     */
    public void updateRateLimit(int rateLimit, int burstCapacity) {
        validateRateLimit(rateLimit);
        validateBurstCapacity(burstCapacity);
        this.rateLimit = rateLimit;
        this.burstCapacity = burstCapacity;
    }

    /**
     * 更新突发容量
     */
    public void updateBurstCapacity(int burstCapacity) {
        validateBurstCapacity(burstCapacity);
        this.burstCapacity = burstCapacity;
    }

    /**
     * 记录访问时间
     */
    public void recordAccess() {
        this.lastAccessTime = Instant.now();
    }

    /**
     * 验证应用是否过期
     */
    public boolean isExpired() {
        return expiryTime != null && Instant.now().isAfter(expiryTime);
    }

    /**
     * 验证IP格式
     */
    private void validateIp(String ip) {
        if (ip == null || !IP_PATTERN.matcher(ip).matches()) {
            throw new AppException("无效的IP地址格式: " + ip);
        }
    }

    /**
     * 验证速率限制
     */
    private void validateRateLimit(int rateLimit) {
        if (rateLimit <= 0) {
            throw new AppException("速率限制必须大于0");
        }
    }

    /**
     * 验证突发容量
     */
    private void validateBurstCapacity(int burstCapacity) {
        if (burstCapacity < rateLimit) {
            throw new AppException("突发容量不能小于速率限制");
        }
    }

    public AppId getId() {
        return appId;
    }

    public Set<EndpointId> getAllowedEndpoints() {
        return Collections.unmodifiableSet(allowedEndpoints);
    }

    public Set<String> getIpWhitelist() {
        return Collections.unmodifiableSet(ipWhitelist);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppAccess appAccess = (AppAccess) o;
        return Objects.equals(appId, appAccess.appId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appId);
    }

}
