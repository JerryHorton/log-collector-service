package cn.cug.sxy.domain.reception.model.valobj;

import cn.cug.sxy.types.model.ValueObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

/**
 * @version 1.0
 * @Date 2025/7/7 11:54
 * @Description 接收结果值对象
 * @Author jerryhotton
 */

@Builder
@Getter
@AllArgsConstructor
public class ReceptionResult implements ValueObject {

    /**
     * 是否成功
     */
    private final boolean success;

    /**
     * 消息
     */
    private final String message;

    /**
     * 批次ID（可能为空，例如单条日志缓冲的情况）
     */
    private final BatchId batchId;

    /**
     * 是否已缓冲（未立即处理）
     */
    private final boolean buffered;

    /**
     * 创建成功结果（带批次ID）
     *
     * @param batchId 批次ID
     * @return 成功结果
     */
    public static ReceptionResult success(BatchId batchId) {
        return new ReceptionResult(true, "接收成功", batchId, false);
    }

    /**
     * 创建成功结果（已缓冲，无批次ID）
     *
     * @return 成功结果
     */
    public static ReceptionResult buffered() {
        return new ReceptionResult(true, "接收成功（已缓冲）", null, true);
    }

    /**
     * 创建失败结果
     *
     * @param message 错误消息
     * @return 失败结果
     */
    public static ReceptionResult failure(String message) {
        return new ReceptionResult(false, message, null, false);
    }

    /**
     * 是否有批次ID
     */
    public boolean hasBatchId() {
        return batchId != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReceptionResult that = (ReceptionResult) o;
        return success == that.success &&
                buffered == that.buffered &&
                Objects.equals(message, that.message) &&
                Objects.equals(batchId, that.batchId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, message, batchId, buffered);
    }

}
