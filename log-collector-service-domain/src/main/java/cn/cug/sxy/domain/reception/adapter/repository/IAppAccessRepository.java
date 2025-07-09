package cn.cug.sxy.domain.reception.adapter.repository;

import cn.cug.sxy.domain.reception.model.aggregate.AppAccess;
import cn.cug.sxy.domain.reception.model.valobj.AccessKey;
import cn.cug.sxy.domain.reception.model.valobj.AppAccessStatus;
import cn.cug.sxy.domain.reception.model.valobj.AppId;

import java.util.List;
import java.util.Optional;

/**
 * @version 1.0
 * @Date 2025/7/7 15:06
 * @Description 应用接入仓储接口（负责应用接入实体的持久化操作）
 * @Author jerryhotton
 */

public interface IAppAccessRepository {

    /**
     * 保存应用接入
     * @param appAccess 应用接入实体
     */
    void save(AppAccess appAccess);

    /**
     * 根据应用ID查找接入信息
     * @param appId 应用ID
     * @return 应用接入实体
     */
    Optional<AppAccess> findByAppId(AppId appId);

    /**
     * 根据访问密钥查找接入信息
     * @param accessKey 访问密钥
     * @return 应用接入实体
     */
    Optional<AppAccess> findByAccessKey(AccessKey accessKey);

    /**
     * 根据状态查找应用接入列表
     * @param status 接入状态
     * @return 应用接入列表
     */
    List<AppAccess> findByStatus(AppAccessStatus status);

    /**
     * 更新应用接入状态
     * @param appId 应用ID
     * @param status 新状态
     */
    void updateStatus(AppId appId, AppAccessStatus status);

    /**
     * 删除应用接入
     * @param appId 应用ID
     */
    void delete(AppId appId);

}
