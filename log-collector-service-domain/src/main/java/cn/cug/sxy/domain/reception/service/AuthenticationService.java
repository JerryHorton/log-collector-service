package cn.cug.sxy.domain.reception.service;

import cn.cug.sxy.domain.reception.model.valobj.AppId;
import cn.cug.sxy.domain.reception.model.valobj.AuthRequest;
import cn.cug.sxy.domain.reception.model.valobj.AuthResult;
import cn.cug.sxy.domain.reception.model.valobj.EndpointId;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @Date 2025/7/7 17:12
 * @Description 认证与授权服务实现
 * @Author jerryhotton
 */

@Service
public class AuthenticationService implements IAuthenticationService {

    // 请求时间戳有效期（秒）
    private static final long TIMESTAMP_VALIDITY_SECONDS = 300;

    // 认证算法
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    @Override
    public AuthResult authenticate(AuthRequest authRequest) {
        return null;
    }

    @Override
    public boolean authorize(AppId appId, EndpointId endpointId, String clientIp) {
        return false;
    }

    @Override
    public boolean isIpWhitelisted(AppId appId, String clientIp) {
        return false;
    }

}
