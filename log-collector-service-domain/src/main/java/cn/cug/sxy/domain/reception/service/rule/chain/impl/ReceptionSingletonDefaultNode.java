package cn.cug.sxy.domain.reception.service.rule.chain.impl;

import cn.cug.sxy.domain.reception.model.entity.ReceptionDynamicContext;
import cn.cug.sxy.domain.reception.model.valobj.*;
import cn.cug.sxy.domain.reception.service.buffer.LogBufferManager;
import cn.cug.sxy.domain.reception.service.metrics.LogProcessingMetrics;
import cn.cug.sxy.types.framework.chain.AbstractLogicChainNode;
import cn.cug.sxy.domain.reception.service.rule.chain.factory.DefaultLogicChainFactory;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @Date 2025/7/9 09:36
 * @Description 默认节点（单条接收）
 * @Author jerryhotton
 */

@Slf4j
@Component(value = "reception_singleton_default_node")
public class ReceptionSingletonDefaultNode extends AbstractLogicChainNode<ReceptionRequest, ReceptionResult, ReceptionDynamicContext> {

    private final LogBufferManager logBufferManager;
    private final LogProcessingMetrics metrics;

    public ReceptionSingletonDefaultNode(LogBufferManager logBufferManager, LogProcessingMetrics metrics) {
        this.logBufferManager = logBufferManager;
        this.metrics = metrics;
    }

    @Override
    public ReceptionResult logic(ReceptionRequest request, ReceptionDynamicContext context) {
        String appId = request.getAppId();
        String endpointId = request.getEndpointId();
        RawLog rawLog = request.getRawLog().get(0);
        log.info("日志接收责任链-默认节点接管 request:{}", JSON.toJSONString(request));
        // 1. 添加日志到缓冲区
        boolean flushed = logBufferManager.addLog(rawLog, appId, endpointId);
        // 2. 记录监控指标
        metrics.recordLogReceived(appId, endpointId, 1);
        // 3. 返回结果
        log.debug("接收日志成功: appId={}, endpointId={}, flushed={}",
                appId, endpointId, flushed);
        log.info("日志接收责任链-默认节点放行 request:{}", JSON.toJSONString(request));

        return ReceptionResult.buffered();
    }

    @Override
    protected String ruleNode() {
        return DefaultLogicChainFactory.NodeType.RECEPTION_SINGLETON_DEFAULT_NODE.getCode();
    }

}
