package cn.cug.sxy.domain.auth.model.entity;

import cn.cug.sxy.domain.auth.model.valobj.AccessKey;
import cn.cug.sxy.domain.auth.model.valobj.AppAccessStatus;
import cn.cug.sxy.domain.auth.model.valobj.AppId;
import cn.cug.sxy.types.model.Entity;
import lombok.Data;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @version 1.0
 * @Date 2025/7/9 19:44
 * @Description 应用访问实体
 * @Author jerryhotton
 */

@Data
public class AppAccess implements Entity<AppId> {

    /**
     * 应用ID
     */
    private final AppId id;
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
     * 创建时间
     */
    private final Instant creationTime;
    /**
     * 过期时间
     */
    private Instant expiryTime;
    /**
     * 状态
     */
    private AppAccessStatus status;
    /**
     * IP白名单
     */
    private Set<String> ipWhitelist;
    /**
     * 允许访问的端点ID列表
     */
    private Set<String> allowedEndpoints;
    /**
     * 访问频率限制（每分钟请求数）
     */
    private int rateLimit;
    /**
     * 突发容量（允许的短时间内最大请求数）
     */
    private int burstCapacity;
    /**
     * 创建一个新的应用访问实体
     */
    public AppAccess(AppId id, String appName, AccessKey accessKey, String secretKey, int rateLimit) {
        this.id = id;
        this.appName = appName;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.rateLimit = rateLimit;
        this.burstCapacity = rateLimit * 2; // 默认突发容量为限制的两倍
        this.creationTime = Instant.now();
        this.expiryTime = Instant.now().plusSeconds(31536000); // 默认一年有效期
        this.status = AppAccessStatus.INACTIVE;
        this.ipWhitelist = new HashSet<>();
        this.allowedEndpoints = new HashSet<>();
    }

    /**
     * 创建一个完整的应用访问实体
     */
    public AppAccess(AppId id, String appName, AccessKey accessKey, String secretKey,
                     int rateLimit, int burstCapacity, Instant expiryTime,
                     Set<String> ipWhitelist, Set<String> allowedEndpoints) {
        this.id = id;
        this.appName = appName;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.rateLimit = rateLimit;
        this.burstCapacity = burstCapacity;
        this.creationTime = Instant.now();
        this.expiryTime = expiryTime;
        this.status = AppAccessStatus.INACTIVE;
        this.ipWhitelist = new HashSet<>(ipWhitelist);
        this.allowedEndpoints = new HashSet<>(allowedEndpoints);
    }

    /**
     * 激活应用访问
     */
    public void activate() {
        if (status == AppAccessStatus.BLOCKED) {
            throw new IllegalStateException("被封禁的应用无法激活");
        }
        this.status = AppAccessStatus.ACTIVE;
    }

    /**
     * 停用应用访问
     */
    public void deactivate() {
        if (status == AppAccessStatus.BLOCKED) {
            throw new IllegalStateException("被封禁的应用无法停用");
        }
        this.status = AppAccessStatus.INACTIVE;
    }

    /**
     * 封禁应用访问
     */
    public void block() {
        this.status = AppAccessStatus.BLOCKED;
    }

    /**
     * 更新应用名称
     */
    public void updateAppName(String appName) {
        this.appName = appName;
    }

    /**
     * 更新密钥
     */
    public void updateSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * 更新过期时间
     */
    public void updateExpiryTime(Instant expiryTime) {
        this.expiryTime = expiryTime;
    }

    /**
     * 更新访问频率限制
     */
    public void updateRateLimit(int rateLimit) {
        this.rateLimit = rateLimit;
    }

    /**
     * 更新突发容量
     */
    public void updateBurstCapacity(int burstCapacity) {
        this.burstCapacity = burstCapacity;
    }

    /**
     * 添加IP到白名单
     */
    public void addIpToWhitelist(String ip) {
        this.ipWhitelist.add(ip);
    }

    /**
     * 从白名单中移除IP
     */
    public void removeIpFromWhitelist(String ip) {
        this.ipWhitelist.remove(ip);
    }

    /**
     * 清空IP白名单
     */
    public void clearIpWhitelist() {
        this.ipWhitelist.clear();
    }

    /**
     * 添加允许访问的端点
     */
    public void addAllowedEndpoint(String endpointId) {
        this.allowedEndpoints.add(endpointId);
    }

    /**
     * 移除允许访问的端点
     */
    public void removeAllowedEndpoint(String endpointId) {
        this.allowedEndpoints.remove(endpointId);
    }

    /**
     * 清空允许访问的端点列表
     */
    public void clearAllowedEndpoints() {
        this.allowedEndpoints.clear();
    }

    /**
     * 检查应用是否已过期
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiryTime);
    }

    /**
     * 检查IP是否在白名单中
     */
    public boolean isIpWhitelisted(String ip) {
        // 如果白名单为空，则允许所有IP
        if (ipWhitelist.isEmpty()) {
            return true;
        }
        return ipWhitelist.contains(ip);
    }

    /**
     * 检查端点是否允许访问
     */
    public boolean isEndpointAllowed(String endpointId) {
        // 如果允许的端点列表为空，则允许访问所有端点
        if (allowedEndpoints.isEmpty()) {
            return true;
        }
        return allowedEndpoints.contains(endpointId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppAccess appAccess = (AppAccess) o;
        return Objects.equals(id, appAccess.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
