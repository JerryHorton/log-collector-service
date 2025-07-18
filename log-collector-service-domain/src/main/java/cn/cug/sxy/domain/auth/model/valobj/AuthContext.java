package cn.cug.sxy.domain.auth.model.valobj;

import cn.cug.sxy.types.model.ValueObject;
import lombok.Getter;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @version 1.0
 * @Date 2025/7/10 10:18
 * @Description 认证上下文值对象（包含已认证的身份信息和上下文数据，用于后续授权决策）
 * @Author jerryhotton
 */

@Getter
public class AuthContext implements ValueObject {

    /**
     * 应用ID
     */
    private final AppId appId;
    /**
     * 客户端IP
     */
    private final String clientIp;
    /**
     * 认证时间
     */
    private final Instant authenticatedAt;
    /**
     * 认证方法
     */
    private final AuthMethod authMethod;
    /**
     * 应用状态
     */
    private final AppAccessStatus appStatus;
    /**
     * 允许访问的端点集合
     */
    private final Set<String> allowedEndpoints;
    /**
     * 应用的IP白名单
     */
    private final Set<String> ipWhitelist;
    /**
     * 访问频率限制（每分钟请求数）
     */
    private final int rateLimit;
    /**
     * 突发容量（允许的短时间内最大请求数）
     */
    private final int burstCapacity;

    public AuthContext(AppId appId, String clientIp, AuthMethod authMethod,
                       AppAccessStatus appStatus, Set<String> allowedEndpoints,
                       Set<String> ipWhitelist, int rateLimit, int burstCapacity) {
        this.appId = appId;
        this.clientIp = clientIp;
        this.authenticatedAt = Instant.now();
        this.authMethod = authMethod;
        this.appStatus = appStatus;
        this.allowedEndpoints = Collections.unmodifiableSet(new HashSet<>(allowedEndpoints));
        this.ipWhitelist = Collections.unmodifiableSet(new HashSet<>(ipWhitelist));
        this.rateLimit = rateLimit;
        this.burstCapacity = burstCapacity;
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

    /**
     * 检查应用是否处于活跃状态
     */
    public boolean isActive() {
        return appStatus == AppAccessStatus.ACTIVE;
    }

}
