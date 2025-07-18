package cn.cug.sxy.infrastructure.dao;

import cn.cug.sxy.infrastructure.dao.po.LogAppAccess;
import org.apache.ibatis.annotations.Mapper;

/**
 * @version 1.0
 * @Date 2025/7/8 09:45
 * @Description 日志应用访问数据访问层接口
 * @Author jerryhotton
 */

@Mapper
public interface ILogAppAccessDao {

    LogAppAccess selectById(String appId);

    LogAppAccess selectByAccessKey(String accessKey);

}
