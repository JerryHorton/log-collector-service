package cn.cug.sxy.domain.reception.model.valobj;

import cn.cug.sxy.types.model.Identifier;

import java.util.Objects;

/**
 * @version 1.0
 * @Date 2025/7/7 11:46
 * @Description 端点ID值对象
 * @Author jerryhotton
 */

public class EndpointId implements Identifier {

    private final String value;

    public EndpointId(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EndpointId that = (EndpointId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }

}
