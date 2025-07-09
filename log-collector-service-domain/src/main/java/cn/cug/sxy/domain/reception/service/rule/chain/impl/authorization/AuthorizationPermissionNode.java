package cn.cug.sxy.domain.reception.service.rule.chain.impl.authorization;

import cn.cug.sxy.domain.reception.model.aggregate.AppAccess;
import cn.cug.sxy.domain.reception.model.entity.AuthorizationDynamicContext;
import cn.cug.sxy.domain.reception.model.valobj.AppId;
import cn.cug.sxy.domain.reception.model.valobj.AuthorizationRequest;
import cn.cug.sxy.domain.reception.model.valobj.AuthorizationResult;
import cn.cug.sxy.domain.reception.model.valobj.EndpointId;
import cn.cug.sxy.domain.reception.service.rule.chain.AbstractLogicChainNode;
import cn.cug.sxy.domain.reception.service.rule.chain.factory.DefaultLogicChainFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @Date 2025/7/8 11:17
 * @Description IP及端点访问权限校验节点
 * @Author jerryhotton
 */

@Slf4j
@Component(value = "authorization_permission_node")
public class AuthorizationPermissionNode extends AbstractLogicChainNode<AuthorizationRequest, AuthorizationResult, AuthorizationDynamicContext> {

    @Override
    public AuthorizationResult logic(AuthorizationRequest request, AuthorizationDynamicContext context) {
        String clientIp = request.getClientIp();
        AppId appId = request.getAppId();
        EndpointId endpointId = request.getEndpointId();
        AppAccess appAccess = context.getAppAccess();
        // 1. 验证IP白名单
        if (!clientIp.isEmpty() && !appAccess.isIpWhitelisted(clientIp)) {
            log.warn("授权失败: IP不在白名单中, appId={}, clientIp={}",
                    appId.getValue(), clientIp);
            return new AuthorizationResult(false);
        }

        // 2. 验证端点访问权限
        if (!appAccess.isEndpointAllowed(endpointId)) {
            log.warn("授权失败: 应用无权访问该端点, appId={}, endpointId={}",
                    appId.getValue(), endpointId.getValue());
            return new AuthorizationResult(false);
        }

        return new AuthorizationResult(true);
    }

    @Override
    protected String ruleNode() {
        return DefaultLogicChainFactory.NodeType.AUTHORIZATION_PERMISSION_NODE.getCode();
    }

}
