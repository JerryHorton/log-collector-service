package cn.cug.sxy.infrastructure.elastic;

import cn.cug.sxy.domain.storage.model.entity.LogDocument;
import cn.cug.sxy.domain.storage.model.valobj.LogQuery;
import cn.cug.sxy.domain.storage.model.valobj.LogQueryResult;

/**
 * @version 1.0
 * @Date 2025/7/15 08:49
 * @Description Elasticsearch日志查询服务接口
 * @Author jerryhotton
 */

public interface IElasticsearchLogQueryService {

    /**
     * 根据查询条件查询日志
     *
     * @param query 日志查询条件
     * @return 日志查询结果
     */
    LogQueryResult queryLogs(LogQuery query);

    /**
     * 根据日志ID获取单条日志
     *
     * @param logId 日志ID
     * @return 日志文档，如果不存在则返回null
     */
    LogDocument getLogById(String logId);

}
