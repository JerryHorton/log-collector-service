package cn.cug.sxy.domain.reception.model.valobj;

import cn.cug.sxy.types.model.ValueObject;
import lombok.Getter;

import java.util.Objects;

/**
 * @version 1.0
 * @Date 2025/7/8 09:29
 * @Description 权限校验结果值对象
 * @Author jerryhotton
 */

@Getter
public class AuthorizationResult implements ValueObject {

    /**
     * 是否已授权
     */
    private final boolean authorized;

    public AuthorizationResult(boolean authorized) {
        this.authorized = authorized;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthorizationResult that = (AuthorizationResult) o;
        return authorized == that.authorized;
    }

    @Override
    public int hashCode() {
        return Objects.hash(authorized);
    }

}
