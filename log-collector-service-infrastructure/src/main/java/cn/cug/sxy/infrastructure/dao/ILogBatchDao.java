package cn.cug.sxy.infrastructure.dao;

import cn.cug.sxy.infrastructure.dao.po.LogBatch;
import org.apache.ibatis.annotations.Mapper;

/**
 * @version 1.0
 * @Date 2025/7/11 16:00
 * @Description 日志批次数据访问层接口
 * @Author jerryhotton
 */

@Mapper
public interface ILogBatchDao {

    void save(LogBatch logBatch);

    void updateBatchMetadata(LogBatch logBatch);

    LogBatch selectById(String batchId);

}
