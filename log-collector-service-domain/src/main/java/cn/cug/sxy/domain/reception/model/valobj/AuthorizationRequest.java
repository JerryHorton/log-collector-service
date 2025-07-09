package cn.cug.sxy.domain.reception.model.valobj;

import cn.cug.sxy.types.model.ValueObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

/**
 * @version 1.0
 * @Date 2025/7/8 09:22
 * @Description 权限校验请求值对象
 * @Author jerryhotton
 */

@Builder
@Getter
@AllArgsConstructor
public class AuthorizationRequest implements ValueObject {

    private AppId appId;

    private EndpointId endpointId;

    private String clientIp;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthorizationRequest that = (AuthorizationRequest) o;
        return Objects.equals(appId, that.appId) &&
                Objects.equals(endpointId, that.endpointId) &&
                Objects.equals(clientIp, that.clientIp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appId, endpointId, clientIp);
    }

}
