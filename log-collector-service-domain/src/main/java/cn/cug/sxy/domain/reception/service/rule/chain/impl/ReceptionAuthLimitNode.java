package cn.cug.sxy.domain.reception.service.rule.chain.impl;

import cn.cug.sxy.domain.reception.adapter.repository.ILogBatchRepository;
import cn.cug.sxy.domain.reception.alc.IAuthGateway;
import cn.cug.sxy.domain.reception.model.entity.ReceptionDynamicContext;
import cn.cug.sxy.domain.reception.model.valobj.*;
import cn.cug.sxy.types.framework.chain.AbstractLogicChainNode;
import cn.cug.sxy.domain.reception.service.rule.chain.factory.DefaultLogicChainFactory;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @Date 2025/7/7 16:58
 * @Description 应用权限与限流校验节点
 * @Author jerryhotton
 */

@Slf4j
@Component(value = "reception_auth_limit_node")
public class ReceptionAuthLimitNode extends AbstractLogicChainNode<ReceptionRequest, ReceptionResult, ReceptionDynamicContext> {

    private final ILogBatchRepository logBatchRepository;
    private final IAuthGateway authGateway;

    public ReceptionAuthLimitNode(ILogBatchRepository logBatchRepository, IAuthGateway authGateway) {
        this.logBatchRepository = logBatchRepository;
        this.authGateway = authGateway;
    }

    @Override
    public ReceptionResult logic(ReceptionRequest request, ReceptionDynamicContext context) {
        RawLog rawLog = request.getRawLog().get(0);
        String appId = request.getAppId();
        String endpointId = request.getEndpointId();
        String clientIp = rawLog.getMetadata().getOrDefault("clientIp", "");

        log.info("日志接收责任链-应用权限与限流校验节点接管 request:{}", JSON.toJSONString(request));
        // 1. 验证应用是否有权限访问该端点
        if (!authGateway.authorize(appId, endpointId, clientIp)) {
            log.warn("接收日志失败: 应用无权访问该端点, appId={}, endpointId={}",
                    appId, endpointId);
            return ReceptionResult.failure("应用无权访问该端点");
        }
        // 2. 检查是否在IP白名单当中
        boolean ipWhitelisted = authGateway.isIpWhitelisted(appId, clientIp);
        if (!ipWhitelisted) {
            log.warn("接收日志失败: IP不在白名单中, appId={}, clientIp={}",
                    appId, clientIp);
            return ReceptionResult.failure("IP不在白名单中");
        }
        // 3. 检查应用是否被限流
        if (authGateway.isRateLimited(appId, endpointId)) {
            log.warn("接收日志失败: 应用被限流, appId={}", appId);
            return ReceptionResult.failure("应用被限流");
        }
        log.info("日志接收责任链-应用权限与限流校验节点放行 request:{}", JSON.toJSONString(request));

        return next().logic(request, context);
    }

    @Override
    protected String ruleNode() {
        return DefaultLogicChainFactory.NodeType.RECEPTION_AUTH_LIMIT_NODE.getCode();
    }

}
