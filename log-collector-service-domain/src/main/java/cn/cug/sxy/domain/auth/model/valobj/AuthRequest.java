package cn.cug.sxy.domain.auth.model.valobj;

import cn.cug.sxy.types.model.ValueObject;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @version 1.0
 * @Date 2025/7/7 13:38
 * @Description 认证请求值对象
 * @Author jerryhotton
 */

@Getter
public class AuthRequest implements ValueObject {

    /** 访问密钥 */
    private final String accessKey;
    /** 签名 */
    private final String signature;
    /** 时间戳 */
    private final String timestamp;
    /** 客户端IP */
    private final String clientIp;
    /** 认证方式 */
    private final AuthMethod authMethod;
    /** 请求头信息 */
    private final Map<String, String> headers;
    /** 请求体 */
    private final String requestBody;

    public AuthRequest(String accessKey, String signature, String timestamp,
                       String clientIp, AuthMethod authMethod,
                       Map<String, String> headers, String requestBody) {
        this.accessKey = accessKey;
        this.signature = signature;
        this.timestamp = timestamp;
        this.clientIp = clientIp;
        this.authMethod = authMethod;
        this.headers = new HashMap<>(headers);
        this.requestBody = requestBody;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthRequest that = (AuthRequest) o;
        return Objects.equals(accessKey, that.accessKey) &&
                Objects.equals(signature, that.signature) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(clientIp, that.clientIp) &&
                authMethod == that.authMethod;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessKey, signature, timestamp, clientIp, authMethod);
    }

}
