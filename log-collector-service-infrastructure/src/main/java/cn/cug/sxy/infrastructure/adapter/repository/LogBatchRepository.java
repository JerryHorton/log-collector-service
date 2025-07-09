package cn.cug.sxy.infrastructure.adapter.repository;

import cn.cug.sxy.domain.reception.adapter.repository.ILogBatchRepository;
import cn.cug.sxy.domain.reception.model.entity.LogBatch;
import cn.cug.sxy.domain.reception.model.valobj.AppId;
import cn.cug.sxy.domain.reception.model.valobj.BatchId;
import cn.cug.sxy.domain.reception.model.valobj.BatchStatus;
import cn.cug.sxy.types.model.Page;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @version 1.0
 * @Date 2025/7/8 19:08
 * @Description 日志批次仓储实现
 * @Author jerryhotton
 */

@Repository
public class LogBatchRepository implements ILogBatchRepository {

    @Override
    public void save(LogBatch logBatch) {

    }

    @Override
    public Optional<LogBatch> findById(BatchId batchId) {
        return Optional.empty();
    }

    @Override
    public List<LogBatch> findByAppIdAndStatus(AppId appId, BatchStatus status) {
        return List.of();
    }

    @Override
    public Page<LogBatch> findByTimeRangeAndStatus(Instant startTime, Instant endTime, BatchStatus status, int pageNumber, int pageSize) {
        return null;
    }

    @Override
    public void updateStatus(BatchId batchId, BatchStatus status, String errorMessage) {

    }

    @Override
    public int deleteExpired(Instant beforeTime) {
        return 0;
    }

    @Override
    public List<LogBatch> findByStatusAndProcessedTimeBefore(BatchStatus status, Instant before) {
        return List.of();
    }

    @Override
    public List<LogBatch> findByStatusAndLastProcessTimeBefore(BatchStatus status, Instant before) {
        return List.of();
    }

    @Override
    public void updateStatusAndProcessTime(BatchId batchId, BatchStatus status, Instant processedTime, Instant lastProcessTime) {

    }

    @Override
    public void updateStatusAndRetryCount(BatchId batchId, BatchStatus status, int retryCount, Instant lastProcessTime) {

    }

    @Override
    public void updateProcessedBatchStatus(BatchId batchId, BatchStatus status, Instant processedTime, Instant lastProcessTime, String batchTraceId, boolean confirmed) {

    }

    @Override
    public void delete(LogBatch batch) {

    }

    @Override
    public List<LogBatch> findAll() {
        return List.of();
    }

    @Override
    public Map<BatchStatus, Long> countByStatus() {
        return Map.of();
    }
}
