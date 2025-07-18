package cn.cug.sxy.infrastructure.adapter.repository;

import cn.cug.sxy.domain.reception.adapter.repository.IReceiverEndpointRepository;
import cn.cug.sxy.domain.reception.model.aggregate.ReceiverEndpoint;
import cn.cug.sxy.domain.reception.model.valobj.EndpointId;
import cn.cug.sxy.domain.reception.model.valobj.EndpointStatus;
import cn.cug.sxy.domain.reception.model.valobj.EndpointType;
import cn.cug.sxy.domain.reception.model.valobj.LogFormat;
import cn.cug.sxy.infrastructure.dao.ILogReceiverEndpointDao;
import cn.cug.sxy.infrastructure.dao.po.LogReceiverEndpoint;
import cn.cug.sxy.types.common.Constants;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @version 1.0
 * @Date 2025/7/7 16:05
 * @Description 接收端点仓储实现
 * @Author jerryhotton
 */

@Repository
public class ReceiverEndpointRepository extends AbstractRepository implements IReceiverEndpointRepository {

    @Resource
    private ILogReceiverEndpointDao receiverEndpointDao;

    @Override
    public void save(ReceiverEndpoint endpoint) {

    }

    @Override
    public Optional<ReceiverEndpoint> findById(EndpointId endpointId) {
        if (null == endpointId) {
            return Optional.empty();
        }
        String cacheKey = Constants.RedisKey.RECEIVER_ENDPOINT_KEY + endpointId.getValue();
        LogReceiverEndpoint po = getDataFromCacheOrDB(cacheKey, () -> receiverEndpointDao.selectById(endpointId.getValue()));
        if (null == po) {
            return Optional.empty();
        }
        ReceiverEndpoint entity = convertToDomainEntity(po);

        return Optional.of(entity);
    }

    @Override
    public List<ReceiverEndpoint> findByType(EndpointType type) {
        return List.of();
    }

    @Override
    public List<ReceiverEndpoint> findByStatus(EndpointStatus status) {
        return List.of();
    }

    @Override
    public void delete(EndpointId endpointId) {

    }

    @Override
    public boolean exists(EndpointId endpointId) {
        return false;
    }

    @Override
    public List<ReceiverEndpoint> findAll() {
        return List.of();
    }

    @Override
    public List<ReceiverEndpoint> findByPreprocessStrategy(String strategyType) {
        return List.of();
    }

    @Override
    public List<ReceiverEndpoint> findWithBufferingEnabled() {
        return List.of();
    }

    @Override
    public void saveAll(List<ReceiverEndpoint> endpoints) {

    }

    @Override
    public List<ReceiverEndpoint> findByAllowedAppId(String appId) {
        return List.of();
    }

    /**
     * 将持久化对象转换为领域实体
     */
    private ReceiverEndpoint convertToDomainEntity(LogReceiverEndpoint po) {
        // 创建基本实体
        EndpointId endpointId = new EndpointId(po.getEndpointId());
        EndpointType type = EndpointType.valueOf(po.getType());
        LogFormat format = LogFormat.valueOf(po.getFormat());

        ReceiverEndpoint endpoint = new ReceiverEndpoint(
                endpointId,
                po.getName(),
                type,
                po.getProtocol(),
                po.getPath(),
                po.getPort(),
                format,
                po.getMaxPayloadSize()
        );

        // 设置状态
        if (EndpointStatus.valueOf(po.getStatus()).equals(EndpointStatus.ACTIVE)) {
            endpoint.activate();
        } else {
            endpoint.deactivate();
        }

        // 设置压缩选项
        if (Boolean.TRUE.equals(po.getCompressionEnabled())) {
            endpoint.enableCompression(po.getCompressionAlgorithm());
        }

        // 设置允许的应用ID
        if (po.getAllowedAppIds() != null && !po.getAllowedAppIds().isEmpty()) {
            for (String appId : po.getAllowedAppIds().split(",")) {
                endpoint.allowApp(appId.trim());
            }
        }

        return endpoint;
    }

}
