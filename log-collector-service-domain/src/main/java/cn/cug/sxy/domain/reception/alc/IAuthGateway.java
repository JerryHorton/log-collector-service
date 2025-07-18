package cn.cug.sxy.domain.reception.alc;

import cn.cug.sxy.types.dto.AuthRequestDTO;
import cn.cug.sxy.types.dto.AuthResultDTO;
import cn.cug.sxy.domain.reception.service.config.RateLimitConfig;

/**
 * @version 1.0
 * @Date 2025/7/10 08:54
 * @Description 认证领域防腐层接口
 * @Author jerryhotton
 */

public interface IAuthGateway {

    /**
     * 验证访问请求
     *
     * @param authRequestDTO 认证请求
     * @return 认证结果
     */
    AuthResultDTO authenticate(AuthRequestDTO authRequestDTO);

    /**
     * 验证应用对端点的访问权限
     *
     * @param appId 应用ID
     * @param endpointId 端点ID
     * @param clientIp 客户端IP
     * @return 是否有权限
     */
    boolean authorize(String appId, String endpointId, String clientIp);

    /**
     * 检查IP是否在白名单中
     *
     * @param appId 应用ID
     * @param clientIp 客户端IP
     * @return 是否在白名单中
     */
    boolean isIpWhitelisted(String appId, String clientIp);

    /**
     * 检查应用级别访问频率是否超限
     *
     * @param appId 应用ID
     * @return 是否超限
     */
    boolean isRateLimited(String appId);

    /**
     * 检查应用级别访问频率是否超限（批量）
     *
     * @param appId 应用ID
     * @param count 请求数量
     * @return 是否超限
     */
    boolean isRateLimited(String appId, int count);

    /**
     * 检查端点级别访问频率是否超限
     *
     * @param appId 应用ID
     * @param endpointId 端点ID
     * @return 是否超限
     */
    boolean isRateLimited(String appId, String endpointId);

    /**
     * 检查端点级别访问频率是否超限（批量）
     *
     * @param appId 应用ID
     * @param endpointId 端点ID
     * @param count 请求数量
     * @return 是否超限
     */
    boolean isRateLimited(String appId, String endpointId, int count);

    /**
     * 获取应用的限流配置
     *
     * @param appId 应用ID
     * @param endpointId 端点ID
     * @return 限流配置
     */
    RateLimitConfig getRateLimitConfig(String appId, String endpointId);

}
