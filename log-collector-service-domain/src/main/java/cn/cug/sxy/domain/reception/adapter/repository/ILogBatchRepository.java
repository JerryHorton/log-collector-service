package cn.cug.sxy.domain.reception.adapter.repository;

import cn.cug.sxy.domain.reception.model.entity.LogBatch;
import cn.cug.sxy.domain.reception.model.valobj.AppId;
import cn.cug.sxy.domain.reception.model.valobj.BatchId;
import cn.cug.sxy.domain.reception.model.valobj.BatchStatus;
import cn.cug.sxy.types.model.Page;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @version 1.0
 * @Date 2025/7/7 15:09
 * @Description 日志批次仓储接口（负责日志批次实体的持久化操作）
 * @Author jerryhotton
 */

public interface ILogBatchRepository {

    /**
     * 保存日志批次
     *
     * @param logBatch 日志批次实体
     */
    void save(LogBatch logBatch);

    /**
     * 根据ID查找日志批次
     *
     * @param batchId 批次ID
     * @return 日志批次实体
     */
    Optional<LogBatch> findById(BatchId batchId);

    /**
     * 根据应用ID和状态查找日志批次
     *
     * @param appId  应用ID
     * @param status 批次状态
     * @return 日志批次列表
     */
    List<LogBatch> findByAppIdAndStatus(AppId appId, BatchStatus status);

    /**
     * 根据时间范围和状态查找日志批次
     *
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param status     批次状态
     * @param pageNumber 页码
     * @param pageSize   页大小
     * @return 分页日志批次列表
     */
    Page<LogBatch> findByTimeRangeAndStatus(Instant startTime, Instant endTime,
                                            BatchStatus status, int pageNumber, int pageSize);

    /**
     * 更新批次状态
     *
     * @param batchId      批次ID
     * @param status       新状态
     * @param errorMessage 错误信息(可选)
     */
    void updateStatus(BatchId batchId, BatchStatus status, String errorMessage);

    /**
     * 删除过期批次
     *
     * @param beforeTime 截止时间
     * @return 删除数量
     */
    int deleteExpired(Instant beforeTime);

    /**
     * 根据状态和处理时间查找批次
     *
     * @param status 批次状态
     * @param before 处理时间截止点
     * @return 批次列表
     */
    List<LogBatch> findByStatusAndProcessedTimeBefore(BatchStatus status, Instant before);

    /**
     * 根据状态和最后处理时间查找批次
     *
     * @param status 批次状态
     * @param before 最后处理时间截止点
     * @return 批次列表
     */
    List<LogBatch> findByStatusAndLastProcessTimeBefore(BatchStatus status, Instant before);

    /**
     * 更新批次状态和处理时间
     *
     * @param batchId 批次ID
     * @param status 新状态
     * @param processedTime 处理时间
     * @param lastProcessTime 最后处理时间
     */
    void updateStatusAndProcessTime(BatchId batchId, BatchStatus status,
                                    Instant processedTime, Instant lastProcessTime);

    /**
     * 更新批次状态和重试次数
     *
     * @param batchId 批次ID
     * @param status 新状态
     * @param retryCount 重试次数
     * @param lastProcessTime 最后处理时间
     */
    void updateStatusAndRetryCount(BatchId batchId, BatchStatus status,
                                   int retryCount, Instant lastProcessTime);

    /**
     * 批次处理完成后的状态更新
     *
     * @param batchId 批次ID
     * @param status 新状态
     * @param processedTime 处理时间
     * @param lastProcessTime 最后处理时间
     * @param batchTraceId 批次跟踪ID
     * @param confirmed 是否已确认
     */
    void updateProcessedBatchStatus(BatchId batchId, BatchStatus status,
                                    Instant processedTime, Instant lastProcessTime,
                                    String batchTraceId, boolean confirmed);

    /**
     * 删除批次
     *
     * @param batch 批次
     */
    void delete(LogBatch batch);

    /**
     * 查询所有批次
     *
     * @return 所有批次列表
     */
    List<LogBatch> findAll();

    /**
     * 统计各状态批次数量
     *
     * @return 状态-数量映射
     */
    Map<BatchStatus, Long> countByStatus();

}
