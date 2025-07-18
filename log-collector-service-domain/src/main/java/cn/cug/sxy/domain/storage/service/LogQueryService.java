package cn.cug.sxy.domain.storage.service;

import cn.cug.sxy.domain.reception.adapter.repository.ILogBatchRepository;
import cn.cug.sxy.domain.storage.model.entity.LogDocument;
import cn.cug.sxy.domain.storage.model.valobj.LogQuery;
import cn.cug.sxy.domain.storage.model.valobj.LogQueryResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @Date 2025/7/15 09:14
 * @Description 日志查询服务实现
 * @Author jerryhotton
 */

@Slf4j
@Service
public class LogQueryService implements ILogQueryService {

    private final ILogBatchRepository logBatchRepository;

    public LogQueryService(ILogBatchRepository logBatchRepository) {
        this.logBatchRepository = logBatchRepository;
    }

    @Override
    public LogQueryResult queryLogs(LogQuery query) {
        log.info("查询日志: query={}", query);
        LogQueryResult result = logBatchRepository.queryLogs(query);
        log.info("查询日志完成: totalHits={}, pageNumber={}, pageSize={}",
                result.getTotalHits(), result.getPageNumber(), result.getPageSize());
        return result;
    }

    @Override
    public LogDocument getLogById(String logId) {
        log.info("根据ID获取日志: logId={}", logId);
        LogDocument document = logBatchRepository.getLogById(logId);
        if (document != null) {
            log.info("找到日志: logId={}", logId);
        } else {
            log.warn("未找到日志: logId={}", logId);
        }

        return document;
    }

}
