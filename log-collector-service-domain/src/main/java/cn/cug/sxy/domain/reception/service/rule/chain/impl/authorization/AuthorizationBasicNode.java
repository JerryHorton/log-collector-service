package cn.cug.sxy.domain.reception.service.rule.chain.impl.authorization;

import cn.cug.sxy.domain.reception.adapter.repository.IAppAccessRepository;
import cn.cug.sxy.domain.reception.model.aggregate.AppAccess;
import cn.cug.sxy.domain.reception.model.entity.AuthorizationDynamicContext;
import cn.cug.sxy.domain.reception.model.valobj.*;
import cn.cug.sxy.domain.reception.service.rule.chain.AbstractLogicChainNode;
import cn.cug.sxy.domain.reception.service.rule.chain.factory.DefaultLogicChainFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @version 1.0
 * @Date 2025/7/8 09:17
 * @Description 权限基础校验节点
 * @Author jerryhotton
 */

@Slf4j
@Component(value = "authorization_basic_node")
public class AuthorizationBasicNode extends AbstractLogicChainNode<AuthorizationRequest, AuthorizationResult, AuthorizationDynamicContext> {

    @Resource
    private IAppAccessRepository appAccessRepository;

    @Override
    public AuthorizationResult logic(AuthorizationRequest request, AuthorizationDynamicContext context) {
        AppId appId = request.getAppId();
        EndpointId endpointId = request.getEndpointId();
        // 1. 验证参数
        if (appId == null || endpointId == null) {
            log.warn("授权失败: 缺少必要参数");
            return new AuthorizationResult(false);
        }

        // 2. 获取应用接入信息
        Optional<AppAccess> appAccessOpt = appAccessRepository.findByAppId(appId);
        if (!appAccessOpt.isPresent()) {
            log.warn("授权失败: 应用不存在, appId={}", appId.getValue());
            return new AuthorizationResult(false);
        }

        AppAccess appAccess = appAccessOpt.get();

        // 3. 验证应用状态
        if (appAccess.getStatus() != AppAccessStatus.ACTIVE) {
            log.warn("授权失败: 应用状态无效, appId={}, status={}",
                    appId.getValue(), appAccess.getStatus());
            return new AuthorizationResult(false);
        }

        // 4. 验证应用是否过期
        if (appAccess.isExpired()) {
            log.warn("授权失败: 应用已过期, appId={}, expiryTime={}",
                    appId.getValue(), appAccess.getExpiryTime());
            return new AuthorizationResult(false);
        }
        context.setAppAccess(appAccess);

        return next().logic(request, context);
    }

    @Override
    protected String ruleNode() {
        return DefaultLogicChainFactory.NodeType.AUTHORIZATION_BASIC_NODE.getCode();
    }

}
