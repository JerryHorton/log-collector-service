package cn.cug.sxy.infrastructure.adapter.repository;

import cn.cug.sxy.domain.reception.adapter.repository.IAppAccessRepository;
import cn.cug.sxy.domain.reception.model.aggregate.AppAccess;
import cn.cug.sxy.domain.reception.model.valobj.AccessKey;
import cn.cug.sxy.domain.reception.model.valobj.AppAccessStatus;
import cn.cug.sxy.domain.reception.model.valobj.AppId;
import cn.cug.sxy.domain.reception.model.valobj.EndpointId;
import cn.cug.sxy.infrastructure.dao.ILogAppAccessDao;
import cn.cug.sxy.infrastructure.dao.po.LogAppAccess;
import cn.cug.sxy.types.common.Constants;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @version 1.0
 * @Date 2025/7/8 09:38
 * @Description 应用接入仓储实现
 * @Author jerryhotton
 */

@Repository
public class AppAccessRepository extends AbstractRepository implements IAppAccessRepository {

    @Resource
    private ILogAppAccessDao logAppAccessDao;

    @Override
    public void save(AppAccess appAccess) {

    }

    @Override
    public Optional<AppAccess> findByAppId(AppId appId) {
        if (null == appId) {
            return Optional.empty();
        }
        String cacheKey = Constants.RedisKey.LOG_APP_ACCESS_KEY + appId.getValue();
        LogAppAccess po = getDataFromCacheOrDB(cacheKey, () -> logAppAccessDao.selectById(appId.getValue()));
        if (null == po) {
            return Optional.empty();
        }
        AppAccess entity = convertToDomainEntity(po);

        return Optional.of(entity);
    }

    @Override
    public Optional<AppAccess> findByAccessKey(AccessKey accessKey) {
        return Optional.empty();
    }

    @Override
    public List<AppAccess> findByStatus(AppAccessStatus status) {
        return List.of();
    }

    @Override
    public void updateStatus(AppId appId, AppAccessStatus status) {

    }

    @Override
    public void delete(AppId appId) {

    }

    private AppAccess convertToDomainEntity(LogAppAccess po) {
        if (po == null) {
            return null;
        }

        // 创建应用ID
        AppId appId = new AppId(po.getAppId());

        // 创建访问密钥
        AccessKey accessKey = new AccessKey(po.getAccessKey());

        // 创建应用接入聚合根
        AppAccess appAccess = new AppAccess(
                appId,
                po.getAppName(),
                accessKey,
                po.getSecretKey(),
                po.getRateLimit()
        );

        // 设置状态
        switch (po.getStatus()) {
            case "ACTIVE":
                appAccess.activate();
                break;
            case "INACTIVE":
                appAccess.deactivate();
                break;
            case "BLOCKED":
                appAccess.block();
                break;
        }

        // 设置过期时间
        if (po.getExpiryTime() != null) {
            appAccess.setExpiryTime(po.getExpiryTime().toInstant());
        }

        // 设置允许访问的端点
        if (po.getAllowedEndpoints() != null && !po.getAllowedEndpoints().isEmpty()) {
            String[] endpointIds = po.getAllowedEndpoints().split(",");
            for (String endpointId : endpointIds) {
                if (endpointId != null && !endpointId.trim().isEmpty()) {
                    appAccess.allowEndpoint(new EndpointId(endpointId.trim()));
                }
            }
        }

        // 设置IP白名单
        if (po.getIpWhitelist() != null && !po.getIpWhitelist().isEmpty()) {
            String[] ips = po.getIpWhitelist().split(",");
            for (String ip : ips) {
                if (ip != null && !ip.trim().isEmpty()) {
                    appAccess.addIpToWhitelist(ip.trim());
                }
            }
        }

        return appAccess;
    }

}
