package cn.cug.sxy.domain.auth.model.valobj;

import cn.cug.sxy.types.model.ValueObject;
import lombok.Getter;

import java.util.Objects;

/**
 * @version 1.0
 * @Date 2025/7/7 13:41
 * @Description 认证结果值对象
 * @Author jerryhotton
 */

@Getter
public class AuthResult implements ValueObject {

    /**
     * 是否已认证
     */
    private final boolean authenticated;

    /**
     * 认证上下文，仅当认证成功时非空
     */
    private final AuthContext authContext;

    /**
     * 错误代码
     */
    private final String errorCode;

    /**
     * 错误信息
     */
    private final String errorMessage;

    private AuthResult(boolean authenticated, AuthContext authContext,
                       String errorCode, String errorMessage) {
        this.authenticated = authenticated;
        this.authContext = authContext;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * 创建认证成功的结果
     *
     * @param authContext 认证上下文
     * @return 成功的认证结果
     */
    public static AuthResult success(AuthContext authContext) {
        return new AuthResult(true, authContext, null, null);
    }

    /**
     * 创建认证失败的结果
     *
     * @param errorCode    错误代码
     * @param errorMessage 错误消息
     * @return 认证失败的结果
     */
    public static AuthResult authenticationFailed(String errorCode, String errorMessage) {
        return new AuthResult(false, null, errorCode, errorMessage);
    }

    /**
     * 检查认证是否成功
     *
     * @return 是否认证成功
     */
    public boolean isSuccess() {
        return authenticated && authContext != null;
    }

    /**
     * 获取应用ID的字符串值
     *
     * @return 应用ID字符串，如果认证失败则返回null
     */
    public String getAppIdValue() {
        return isSuccess() ? authContext.getAppId().getValue() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthResult that = (AuthResult) o;
        return authenticated == that.authenticated &&
                Objects.equals(authContext, that.authContext) &&
                Objects.equals(errorCode, that.errorCode) &&
                Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authenticated, authContext, errorCode, errorMessage);
    }

}
