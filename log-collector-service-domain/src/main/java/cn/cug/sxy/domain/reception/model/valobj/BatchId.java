package cn.cug.sxy.domain.reception.model.valobj;

import cn.cug.sxy.types.model.Identifier;

import java.util.Objects;
import java.util.UUID;

/**
 * @version 1.0
 * @Date 2025/7/7 11:43
 * @Description 批次ID值对象
 * @Author jerryhotton
 */

public class BatchId implements Identifier {

    private final String value;

    public BatchId(String value) {
        this.value = value;
    }

    /**
     * 生成新的批次ID
     */
    public static BatchId generate() {
        return new BatchId(UUID.randomUUID().toString());
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BatchId batchId = (BatchId) o;
        return Objects.equals(value, batchId.value);
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
