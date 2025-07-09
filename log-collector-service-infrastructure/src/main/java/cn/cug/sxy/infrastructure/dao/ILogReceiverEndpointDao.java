package cn.cug.sxy.infrastructure.dao;

import cn.cug.sxy.infrastructure.dao.po.LogReceiverEndpoint;
import org.apache.ibatis.annotations.Mapper;

/**
 * @version 1.0
 * @Date 2025/7/7 16:26
 * @Description 日志接收端数据访问接口
 * @Author jerryhotton
 */

@Mapper
public interface ILogReceiverEndpointDao {

    LogReceiverEndpoint selectById(String id);

}
