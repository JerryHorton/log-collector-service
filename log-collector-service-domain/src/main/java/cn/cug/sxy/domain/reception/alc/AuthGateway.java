package cn.cug.sxy.domain.reception.alc;

import cn.cug.sxy.domain.auth.model.entity.AppAccess;
import cn.cug.sxy.domain.auth.model.valobj.*;
import cn.cug.sxy.domain.auth.service.IAuthService;
import cn.cug.sxy.domain.auth.service.ratelimit.IRateLimitService;
import cn.cug.sxy.types.dto.AuthRequestDTO;
import cn.cug.sxy.types.dto.AuthResultDTO;
import cn.cug.sxy.domain.reception.service.config.RateLimitConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @version 1.0
 * @Date 2025/7/10 09:19
 * @Description 证领域防腐层实现
 * @Author jerryhotton
 */

@Slf4j
@Component
public class AuthGateway implements IAuthGateway {

    private final IAuthService authService;
    private final IRateLimitService rateLimitService;

    // 存储认证上下文的缓存，用于后续授权检查
    private final Map<String, AuthContext> authContextCache = new HashMap<>();

    public AuthGateway(IAuthService authService, IRateLimitService rateLimitService) {
        this.authService = authService;
        this.rateLimitService = rateLimitService;
    }

    @Override
    public AuthResultDTO authenticate(AuthRequestDTO request) {
        // 将接收领域的认证请求转换为认证领域的认证请求
        AuthRequest authDomainRequest = convertToAuthDomainRequest(request);
        // 调用认证领域服务
        AuthResult result = authService.authenticate(authDomainRequest);
        // 如果认证成功，缓存认证上下文
        if (result.isSuccess()) {
            String appId = result.getAuthContext().getAppId().getValue();
            authContextCache.put(appId, result.getAuthContext());
        }

        // 将认证领域的认证结果转换为接收领域的认证结果
        return convertToReceptionDomainResult(result);
    }

    @Override
    public boolean authorize(String appId, String endpointId, String clientIp) {
        // 获取认证上下文
        AuthContext authContext = getAuthContext(appId);
        if (authContext == null) {
            log.warn("授权失败: 未找到认证上下文, appId={}", appId);
            return false;
        }
        // 创建端点访问权限
        Permission permission = Permission.forEndpoint(endpointId);
        // 调用认证领域服务的授权方法
        return authService.authorize(authContext, permission);
    }

    @Override
    public boolean isIpWhitelisted(String appId, String clientIp) {
        // 调用认证领域服务
        return authService.isIpWhitelisted(new AppId(appId), clientIp);
    }

    @Override
    public boolean isRateLimited(String appId) {
        // 调用认证领域服务
        return rateLimitService.isRateLimited(new AppId(appId));
    }

    @Override
    public boolean isRateLimited(String appId, int count) {
        // 调用认证领域服务的批量限流检查
        return rateLimitService.isRateLimited(new AppId(appId), count);
    }

    @Override
    public boolean isRateLimited(String appId, String endpointId) {
        // 调用认证领域的端点级别限流检查方法
        return rateLimitService.isRateLimited(new AppId(appId), endpointId);
    }

    @Override
    public boolean isRateLimited(String appId, String endpointId, int count) {
        // 调用认证领域的端点级别批量限流检查方法
        return rateLimitService.isRateLimited(new AppId(appId), endpointId, count);
    }

    @Override
    public RateLimitConfig getRateLimitConfig(String appId, String endpointId) {
        // 获取应用信息
        cn.cug.sxy.domain.auth.service.config.RateLimitConfig authConfig =
                rateLimitService.getRateLimitConfig(new AppId(appId), endpointId);

        if (authConfig == null) {
            return null;
        }
        // 创建接收领域的限流配置对象
        return new RateLimitConfig(
                appId,
                endpointId,
                authConfig.getRateLimit(),
                authConfig.getBurstCapacity()
        );
    }

    /**
     * 获取认证上下文
     * 如果缓存中不存在，则尝试从认证服务获取
     */
    private AuthContext getAuthContext(String appId) {
        // 从缓存中获取
        AuthContext authContext = authContextCache.get(appId);
        if (authContext != null) {
            return authContext;
        }
        // 缓存中不存在，从认证服务获取应用信息
        AppAccess appAccess = authService.getAppAccess(new AppId(appId));
        if (appAccess == null) {
            return null;
        }
        // 创建认证上下文
        authContext = new AuthContext(
                appAccess.getId(),
                "", // 客户端IP为空
                AuthMethod.NONE, // 认证方法为空
                appAccess.getStatus(),
                appAccess.getAllowedEndpoints(),
                appAccess.getIpWhitelist(),
                appAccess.getRateLimit(),
                appAccess.getBurstCapacity()
        );
        // 缓存认证上下文
        authContextCache.put(appId, authContext);

        return authContext;
    }

    /**
     * 将接收领域的认证请求转换为认证领域的认证请求
     */
    private AuthRequest convertToAuthDomainRequest(AuthRequestDTO request) {
        // 转换认证方法
        AuthMethod authMethod = AuthMethod.valueOf(request.getAuthMethod().name());
        // 转换请求头
        Map<String, String> headers = new HashMap<>(request.getHeaders());

        return new AuthRequest(
                request.getAccessKey(),
                request.getSignature(),
                request.getTimestamp(),
                request.getClientIp(),
                authMethod,
                headers,
                request.getRequestBody()
        );
    }

    /**
     * 将认证领域的认证结果转换为接收领域的认证结果
     */
    private AuthResultDTO convertToReceptionDomainResult(AuthResult authDomainResult) {
        if (authDomainResult.isSuccess()) {
            String appId = authDomainResult.getAppIdValue();
            return AuthResultDTO.success(appId);
        } else {
            return AuthResultDTO.authenticationFailed(
                    authDomainResult.getErrorCode(),
                    authDomainResult.getErrorMessage()
            );
        }
    }

}
