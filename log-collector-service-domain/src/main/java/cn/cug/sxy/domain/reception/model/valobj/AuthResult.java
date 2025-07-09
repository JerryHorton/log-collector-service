package cn.cug.sxy.domain.reception.model.valobj;

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
     * 是否已授权
     */
    private final boolean authorized;
    /**
     * 应用ID
     */
    private final AppId appId;
    /**
     * 错误代码
     */
    private final String errorCode;
    /**
     * 错误信息
     */
    private final String errorMessage;

    public AuthResult(boolean authenticated, boolean authorized, AppId appId,
                      String errorCode, String errorMessage) {
        this.authenticated = authenticated;
        this.authorized = authorized;
        this.appId = appId;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * 创建认证和授权都成功的结果
     *
     * @param appId 应用ID
     * @return 成功的认证结果
     */
    public static AuthResult success(AppId appId) {
        return new AuthResult(true, true, appId, null, null);
    }

    /**
     * 创建认证失败的结果
     *
     * @param errorCode 错误代码
     * @param errorMessage 错误消息
     * @return 认证失败的结果
     */
    public static AuthResult authenticationFailed(String errorCode, String errorMessage) {
        return new AuthResult(false, false, null, errorCode, errorMessage);
    }

    /**
     * 创建认证成功但授权失败的结果
     *
     * @param appId 应用ID
     * @param errorCode 错误代码
     * @param errorMessage 错误消息
     * @return 授权失败的结果
     */
    public static AuthResult authorizationFailed(AppId appId, String errorCode, String errorMessage) {
        return new AuthResult(true, false, appId, errorCode, errorMessage);
    }

    public boolean isSuccess() {
        return authenticated && authorized;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthResult that = (AuthResult) o;
        return authenticated == that.authenticated &&
                authorized == that.authorized &&
                Objects.equals(appId, that.appId) &&
                Objects.equals(errorCode, that.errorCode) &&
                Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authenticated, authorized, appId, errorCode, errorMessage);
    }

}
