package cn.cug.sxy.domain.reception.service.rule.chain.impl.reception;

import cn.cug.sxy.domain.reception.adapter.repository.ILogBatchRepository;
import cn.cug.sxy.domain.reception.model.entity.ReceptionDynamicContext;
import cn.cug.sxy.domain.reception.model.valobj.*;
import cn.cug.sxy.domain.reception.service.IAuthenticationService;
import cn.cug.sxy.domain.reception.service.IRateLimitService;
import cn.cug.sxy.domain.reception.service.ratelimit.RateLimitService;
import cn.cug.sxy.domain.reception.service.rule.chain.AbstractLogicChainNode;
import cn.cug.sxy.domain.reception.service.rule.chain.factory.DefaultLogicChainFactory;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

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
    private final IAuthenticationService authenticationService;
    private final IRateLimitService rateLimitService;

    public ReceptionAuthLimitNode(ILogBatchRepository logBatchRepository, IAuthenticationService authenticationService, RateLimitService rateLimitService) {
        this.logBatchRepository = logBatchRepository;
        this.authenticationService = authenticationService;
        this.rateLimitService = rateLimitService;
    }

    @Override
    public ReceptionResult logic(ReceptionRequest request, ReceptionDynamicContext context) {
        RawLog rawLog = request.getRawLog().get(0);
        AppId appId = request.getAppId();
        EndpointId endpointId = request.getEndpointId();
        log.info("日志接收责任链-应用权限与限流校验节点接管 request:{}", JSON.toJSONString(request));
        // 1. 验证应用是否有权限访问该端点
        if (!authenticationService.authorize(appId, endpointId, rawLog.getMetadata().getOrDefault("clientIp", ""))) {
            log.warn("接收日志失败: 应用无权访问该端点, appId={}, endpointId={}",
                    appId.getValue(), endpointId.getValue());
            return ReceptionResult.failure("应用无权访问该端点");
        }
        // 2. 检查应用是否被限流
        if (rateLimitService.isRateLimited(appId, endpointId)) {
            log.warn("接收日志失败: 应用被限流, appId={}", appId.getValue());
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
