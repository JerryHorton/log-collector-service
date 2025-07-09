package cn.cug.sxy.domain.reception.model.valobj;

import cn.cug.sxy.types.model.ValueObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

/**
 * @version 1.0
 * @Date 2025/7/7 14:24
 * @Description 接收请求值对象
 * @Author jerryhotton
 */

@Builder
@Getter
@AllArgsConstructor
public class ReceptionRequest implements ValueObject {

    private final List<RawLog> rawLog;

    private final AppId appId;

    private EndpointId endpointId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReceptionRequest that = (ReceptionRequest) o;
        return Objects.equals(rawLog, that.rawLog) &&
                Objects.equals(appId, that.appId) &&
                Objects.equals(endpointId, that.endpointId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rawLog, appId, endpointId);
    }

}
