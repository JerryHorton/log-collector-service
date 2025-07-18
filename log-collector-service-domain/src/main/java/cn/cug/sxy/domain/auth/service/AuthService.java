package cn.cug.sxy.domain.auth.service;

import cn.cug.sxy.domain.auth.model.entity.AppAccess;
import cn.cug.sxy.domain.auth.model.valobj.*;
import cn.cug.sxy.domain.auth.service.cache.AuthorizationCache;
import cn.cug.sxy.domain.reception.adapter.repository.IAppAccessRepository;
import cn.cug.sxy.types.common.Constants;
import cn.cug.sxy.types.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;

/**
 * @version 1.0
 * @Date 2025/7/9 19:43
 * @Description 认证服务实现
 * @Author jerryhotton
 */

@Slf4j
@Service
public class AuthService implements IAuthService {

    // HMAC签名的时间戳有效期（毫秒）
    private static final long TIMESTAMP_VALIDITY_MILLISECONDS = 300000L;
    // HMAC算法
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final IAppAccessRepository appAccessRepository;
    private final AuthorizationCache authorizationCache;

    public AuthService(
            IAppAccessRepository appAccessRepository,
            AuthorizationCache authorizationCache) {  // 注入限流服务
        this.appAccessRepository = appAccessRepository;
        this.authorizationCache = authorizationCache;
    }

    @Override
    public AuthResult authenticate(AuthRequest authRequest) {
        // 1. 根据accessKey查找应用
        Optional<AppAccess> appAccessOpt = appAccessRepository.findByAccessKey(new AccessKey(authRequest.getAccessKey()));
        if (!appAccessOpt.isPresent()) {
            return AuthResult.authenticationFailed(ResponseCode.INVALID_ACCESS_KEY.getCode(), ResponseCode.INVALID_ACCESS_KEY.getInfo());
        }
        AppAccess appAccess = appAccessOpt.get();
        // 2. 验证应用状态
        if (appAccess.getStatus() != AppAccessStatus.ACTIVE) {
            return AuthResult.authenticationFailed(ResponseCode.INVALID_APP_STATUS.getCode(),
                    ResponseCode.INVALID_APP_STATUS.getInfo() + ":" + appAccess.getStatus().getInfo());
        }
        // 3. 验证应用是否过期
        if (appAccess.isExpired()) {
            return AuthResult.authenticationFailed(ResponseCode.APP_ACCESS_EXPIRED.getCode(), ResponseCode.APP_ACCESS_EXPIRED.getInfo());
        }
        // 4. 验证时间戳
        try {
            long timestamp = Long.parseLong(authRequest.getTimestamp());
            long currentTime = System.currentTimeMillis();

            if (Math.abs(currentTime - timestamp) > TIMESTAMP_VALIDITY_MILLISECONDS) {
                return AuthResult.authenticationFailed(ResponseCode.INVALID_TIMESTAMP.getCode(), ResponseCode.INVALID_TIMESTAMP.getInfo());
            }
        } catch (NumberFormatException e) {
            return AuthResult.authenticationFailed(ResponseCode.INVALID_TIMESTAMP_FORMAT.getCode(), ResponseCode.INVALID_TIMESTAMP_FORMAT.getInfo());
        }
        // 5. 验证签名
        if (AuthMethod.HMAC_SIGNATURE.equals(authRequest.getAuthMethod())) {
            boolean signatureValid = verifySignature(
                    authRequest.getSignature(),
                    authRequest.getAccessKey(),
                    appAccess.getSecretKey(),
                    authRequest.getTimestamp(),
                    authRequest.getRequestBody()
            );
            if (!signatureValid) {
                return AuthResult.authenticationFailed(ResponseCode.SIGNATURE_VALIDATION_FAILED.getCode(), ResponseCode.SIGNATURE_VALIDATION_FAILED.getInfo());
            }
        }
        // 6. 身份验证通过，创建认证上下文
        AuthContext authContext = new AuthContext(
                appAccess.getId(),
                authRequest.getClientIp(),
                authRequest.getAuthMethod(),
                appAccess.getStatus(),
                appAccess.getAllowedEndpoints(),
                appAccess.getIpWhitelist(),
                appAccess.getRateLimit(),
                appAccess.getBurstCapacity()
        );

        return AuthResult.success(authContext);
    }

    @Override
    public boolean authorize(AuthContext authContext, Permission permission) {
        // 使用缓存机制，避免频繁计算
        String cacheKey = generateAuthorizationCacheKey(authContext.getAppId(), permission);

        return authorizationCache.getAuthorizationResult(cacheKey, () -> {
            // 1. 验证应用状态
            if (!authContext.isActive()) {
                log.warn("授权失败: 应用状态无效, appId={}", authContext.getAppId().getValue());
                return false;
            }
            // 2. 验证IP白名单
            String clientIp = authContext.getClientIp();
            if (!clientIp.isEmpty() && !authContext.isIpWhitelisted(clientIp)) {
                log.warn("授权失败: IP不在白名单中, appId={}, clientIp={}",
                        authContext.getAppId().getValue(), clientIp);
                return false;
            }
            // 3. 根据权限类型执行不同的授权逻辑
            switch (permission.getType()) {
                case ENDPOINT_ACCESS:
                    return authorizeEndpointInternal(authContext, permission.getResource());
                case API_CALL:
                    return authorizeApiCallInternal(authContext, permission.getResource(), permission.getAction());
                case RESOURCE_OPERATION:
                    return authorizeResourceOperationInternal(authContext, permission.getResource(), permission.getAction());
                case FEATURE_ACCESS:
                    return authorizeFeatureInternal(authContext, permission.getResource());
                default:
                    log.warn("授权失败: 未知的权限类型, appId={}, permissionType={}",
                            authContext.getAppId().getValue(), permission.getType());
                    return false;
            }
        });
    }

    @Override
    public boolean authorizeEndpoint(AuthContext authContext, String endpointId) {
        // 使用通用的authorize方法，传入端点访问权限
        return authorize(authContext, Permission.forEndpoint(endpointId));
    }

    @Override
    public boolean isIpWhitelisted(AppId appId, String clientIp) {
        if (clientIp == null || clientIp.isEmpty()) {
            return true; // 如果客户端IP为空，则默认允许
        }
        Optional<AppAccess> appAccessOpt = appAccessRepository.findByAppId(appId);
        if (!appAccessOpt.isPresent()) {
            return false;
        }
        AppAccess appAccess = appAccessOpt.get();

        return appAccess.isIpWhitelisted(clientIp);
    }

    @Override
    public AppAccess getAppAccess(AppId appId) {
        Optional<AppAccess> appAccessOpt = appAccessRepository.findByAppId(appId);
        return appAccessOpt.orElse(null);
    }

    /**
     * 验证HMAC签名
     */
    private boolean verifySignature(String signature, String accessKey, String secretKey, String timestamp, String payload) {
        try {
            // 构造签名字符串: accessKey + timestamp + payload
            String signatureStr = accessKey + timestamp + payload;
            // 使用HMAC-SHA256算法计算签名
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] hmacData = mac.doFinal(signatureStr.getBytes(StandardCharsets.UTF_8));
            // Base64编码
            String calculatedSignature = Base64.getEncoder().encodeToString(hmacData);
            // 比较签名
            return MessageDigest.isEqual(
                    signature.getBytes(StandardCharsets.UTF_8),
                    calculatedSignature.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("验证签名异常", e);
            return false;
        }
    }

    /**
     * 生成授权缓存键
     */
    private String generateAuthorizationCacheKey(AppId appId, Permission permission) {
        return "auth:" + appId.getValue() + ":" + permission.toString();
    }

    /**
     * 内部方法：授权端点访问
     */
    private boolean authorizeEndpointInternal(AuthContext authContext, String endpointId) {
        boolean isAllowed = authContext.isEndpointAllowed(endpointId);
        if (!isAllowed) {
            log.warn("授权失败: 应用无权访问该端点, appId={}, endpointId={}",
                    authContext.getAppId().getValue(), endpointId);
        }

        return isAllowed;
    }

    /**
     * 内部方法：授权API调用
     */
    private boolean authorizeApiCallInternal(AuthContext authContext, String apiPath, String method) {
        // 这里可以实现更复杂的API调用授权逻辑
        // 简化实现，默认允许
        return true;
    }

    /**
     * 内部方法：授权资源操作
     */
    private boolean authorizeResourceOperationInternal(AuthContext authContext, String resource, String operation) {
        // 这里可以实现更复杂的资源操作授权逻辑
        // 简化实现，默认允许
        return true;
    }

    /**
     * 内部方法：授权功能访问
     */
    private boolean authorizeFeatureInternal(AuthContext authContext, String feature) {
        // 这里可以实现更复杂的功能访问授权逻辑
        // 简化实现，默认允许
        return true;
    }

}
