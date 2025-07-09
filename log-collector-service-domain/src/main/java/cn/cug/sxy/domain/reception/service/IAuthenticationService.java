package cn.cug.sxy.domain.reception.service;

import cn.cug.sxy.domain.reception.model.valobj.AppId;
import cn.cug.sxy.domain.reception.model.valobj.AuthRequest;
import cn.cug.sxy.domain.reception.model.valobj.AuthResult;
import cn.cug.sxy.domain.reception.model.valobj.EndpointId;

/**
 * @version 1.0
 * @Date 2025/7/7 17:10
 * @Description 认证与授权服务接口（负责验证和鉴权日志接入请求）
 * @Author jerryhotton
 */

public interface IAuthenticationService {

    /**
     * 验证访问请求
     *
     * @param authRequest 认证请求
     * @return 认证结果
     */
    AuthResult authenticate(AuthRequest authRequest);

    /**
     * 验证应用对端点的访问权限
     *
     * @param appId 应用ID
     * @param endpointId 端点ID
     * @param clientIp 客户端IP
     * @return 是否有权限
     */
    boolean authorize(AppId appId, EndpointId endpointId, String clientIp);

    /**
     * 检查IP是否在白名单中
     *
     * @param appId 应用ID
     * @param clientIp 客户端IP
     * @return 是否在白名单中
     */
    boolean isIpWhitelisted(AppId appId, String clientIp);

}
