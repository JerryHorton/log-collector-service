package cn.cug.sxy.domain.auth.service;

import cn.cug.sxy.domain.auth.model.entity.AppAccess;
import cn.cug.sxy.domain.auth.model.valobj.*;

/**
 * @version 1.0
 * @Date 2025/7/9 19:41
 * @Description 认证服务接口（提供认证和授权相关的领域服务）
 * @Author jerryhotton
 */

public interface IAuthService {

    /**
     * 验证访问请求，仅执行身份认证
     * <p>
     * 此方法仅负责验证请求者的身份，不检查权限
     *
     * @param authRequest 认证请求
     * @return 认证结果，包含认证上下文
     */
    AuthResult authenticate(AuthRequest authRequest);

    /**
     * 验证应用是否有权限执行特定操作
     * <p>
     * 此方法假设应用身份已经过认证，仅检查权限
     *
     * @param authContext 认证上下文，包含已验证的身份信息
     * @param permission  请求的权限
     * @return 是否有权限
     */
    boolean authorize(AuthContext authContext, Permission permission);

    /**
     * 验证应用对端点的访问权限
     *
     * @param authContext 认证上下文，包含已验证的身份信息
     * @param endpointId  端点ID
     * @return 是否有权限
     */
    boolean authorizeEndpoint(AuthContext authContext, String endpointId);

    /**
     * 检查IP是否在白名单中
     *
     * @param appId    应用ID
     * @param clientIp 客户端IP
     * @return 是否在白名单中
     */
    boolean isIpWhitelisted(AppId appId, String clientIp);

    /**
     * 获取应用访问实体
     *
     * @param appId 应用ID
     * @return 应用访问实体，如果不存在则返回null
     */
    AppAccess getAppAccess(AppId appId);

}
