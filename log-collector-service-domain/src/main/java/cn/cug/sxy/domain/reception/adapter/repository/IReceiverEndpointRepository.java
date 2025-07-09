package cn.cug.sxy.domain.reception.adapter.repository;

import cn.cug.sxy.domain.reception.model.aggregate.ReceiverEndpoint;
import cn.cug.sxy.domain.reception.model.valobj.EndpointId;
import cn.cug.sxy.domain.reception.model.valobj.EndpointStatus;
import cn.cug.sxy.domain.reception.model.valobj.EndpointType;

import java.util.List;
import java.util.Optional;

/**
 * @version 1.0
 * @Date 2025/7/7 13:57
 * @Description 接收端点仓储接口（负责接收端点实体的持久化操作）
 * @Author jerryhotton
 */

public interface IReceiverEndpointRepository {

    /**
     * 保存接收端点
     * @param endpoint 接收端点实体
     */
    void save(ReceiverEndpoint endpoint);

    /**
     * 根据ID查找接收端点
     * @param endpointId 端点ID
     * @return 接收端点实体
     */
    Optional<ReceiverEndpoint> findById(EndpointId endpointId);

    /**
     * 根据类型查找接收端点列表
     * @param type 端点类型
     * @return 接收端点列表
     */
    List<ReceiverEndpoint> findByType(EndpointType type);

    /**
     * 根据状态查找接收端点列表
     * @param status 端点状态
     * @return 接收端点列表
     */
    List<ReceiverEndpoint> findByStatus(EndpointStatus status);

    /**
     * 删除接收端点
     * @param endpointId 端点ID
     */
    void delete(EndpointId endpointId);

    /**
     * 检查端点是否存在
     * @param endpointId 端点ID
     * @return 是否存在
     */
    boolean exists(EndpointId endpointId);

    /**
     * 获取所有接收端点
     * @return 所有接收端点列表
     */
    List<ReceiverEndpoint> findAll();

    /**
     * 根据预处理策略类型查找接收端点
     * @param strategyType 预处理策略类型
     * @return 接收端点列表
     */
    List<ReceiverEndpoint> findByPreprocessStrategy(String strategyType);

    /**
     * 查找启用了缓冲的端点
     * @return 启用了缓冲的端点列表
     */
    List<ReceiverEndpoint> findWithBufferingEnabled();

    /**
     * 批量保存接收端点
     * @param endpoints 接收端点列表
     */
    void saveAll(List<ReceiverEndpoint> endpoints);

    /**
     * 根据应用ID查找允许访问的端点
     * @param appId 应用ID
     * @return 允许访问的端点列表
     */
    List<ReceiverEndpoint> findByAllowedAppId(String appId);

}
