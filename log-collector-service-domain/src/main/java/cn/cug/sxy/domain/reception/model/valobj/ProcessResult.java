package cn.cug.sxy.domain.reception.model.valobj;

import lombok.Getter;

import java.util.Objects;

/**
 * @version 1.0
 * @Date 2025/7/7 11:55
 * @Description 处理结果值对象
 * @Author jerryhotton
 */

@Getter
public class ProcessResult {

    private final boolean success;
    private final String message;
    private final int processedCount;
    private final int failedCount;
    private final long processingTimeMs;

    public ProcessResult(boolean success, String message, int processedCount,
                         int failedCount, long processingTimeMs) {
        this.success = success;
        this.message = message;
        this.processedCount = processedCount;
        this.failedCount = failedCount;
        this.processingTimeMs = processingTimeMs;
    }

    public static ProcessResult success(int processedCount, long processingTimeMs) {
        return new ProcessResult(true, "处理成功", processedCount, 0, processingTimeMs);
    }

    public static ProcessResult partialSuccess(int processedCount, int failedCount, long processingTimeMs) {
        return new ProcessResult(true, "部分处理成功", processedCount, failedCount, processingTimeMs);
    }

    public static ProcessResult failure(String message, int failedCount) {
        return new ProcessResult(false, message, 0, failedCount, 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessResult that = (ProcessResult) o;
        return success == that.success &&
                processedCount == that.processedCount &&
                failedCount == that.failedCount &&
                processingTimeMs == that.processingTimeMs &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, message, processedCount, failedCount, processingTimeMs);
    }

}
