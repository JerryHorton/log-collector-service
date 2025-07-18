package cn.cug.sxy.domain.reception.service.rule.chain.impl;

import cn.cug.sxy.domain.reception.adapter.repository.IReceiverEndpointRepository;
import cn.cug.sxy.domain.reception.model.aggregate.ReceiverEndpoint;
import cn.cug.sxy.domain.reception.model.entity.ReceptionDynamicContext;
import cn.cug.sxy.domain.reception.model.valobj.*;
import cn.cug.sxy.types.framework.chain.AbstractLogicChainNode;
import cn.cug.sxy.domain.reception.service.rule.chain.factory.DefaultLogicChainFactory;
import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * @version 1.0
 * @Date 2025/7/7 14:13
 * @Description 日志接收基础规则节点
 * @Author jerryhotton
 */

@Slf4j
@Component(value = "reception_basic_node")
public class ReceptionBasicNode extends AbstractLogicChainNode<ReceptionRequest, ReceptionResult, ReceptionDynamicContext> {

    @Resource
    private IReceiverEndpointRepository endpointRepository;

    @Override
    public ReceptionResult logic(ReceptionRequest request, ReceptionDynamicContext context) {
        List<RawLog> rawLogs = request.getRawLog();
        String appId = request.getAppId();
        String endpointId = request.getEndpointId();
        log.info("日志接收责任链-日志接收基础规则节点接管 request:{}", JSON.toJSONString(request));
        // 1. 验证参数
        if (rawLogs == null || rawLogs.isEmpty() || appId == null || endpointId == null) {
            log.warn("接收日志失败: 参数不能为空");
            return ReceptionResult.failure("参数不能为空");
        }
        // 2. 验证接收端点是否存在且活跃
        Optional<ReceiverEndpoint> endpointOpt = endpointRepository.findById(new EndpointId(endpointId));
        if (!endpointOpt.isPresent()) {
            log.warn("接收日志失败: 接收端点不存在, endpointId={}", endpointId);
            return ReceptionResult.failure("接收端点不存在");
        }
        ReceiverEndpoint endpoint = endpointOpt.get();
        if (endpoint.getStatus() != EndpointStatus.ACTIVE) {
            log.warn("接收日志失败: 接收端点未激活, endpointId={}, status={}",
                    endpointId, endpoint.getStatus());
            return ReceptionResult.failure("接收端点未激活");
        }
        context.setReceiverEndpoint(endpoint);
        log.info("日志接收责任链-日志接收基础规则节点放行 request:{}", JSON.toJSONString(request));

        return next().logic(request, context);
    }

    @Override
    protected String ruleNode() {
        return DefaultLogicChainFactory.NodeType.RECEPTION_BASIC_NODE.getCode();
    }

}
