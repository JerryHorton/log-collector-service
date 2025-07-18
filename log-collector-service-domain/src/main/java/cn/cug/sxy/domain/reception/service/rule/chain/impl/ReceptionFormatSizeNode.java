package cn.cug.sxy.domain.reception.service.rule.chain.impl;

import cn.cug.sxy.domain.reception.model.aggregate.ReceiverEndpoint;
import cn.cug.sxy.domain.reception.model.entity.ReceptionDynamicContext;
import cn.cug.sxy.domain.reception.model.valobj.*;
import cn.cug.sxy.types.framework.chain.AbstractLogicChainNode;
import cn.cug.sxy.domain.reception.service.rule.chain.factory.DefaultLogicChainFactory;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0
 * @Date 2025/7/7 16:40
 * @Description 日志格式与负载大小校验节点
 * @Author jerryhotton
 */

@Slf4j
@Component(value = "reception_format_size_node")
public class ReceptionFormatSizeNode extends AbstractLogicChainNode<ReceptionRequest, ReceptionResult, ReceptionDynamicContext> {

    @Override
    public ReceptionResult logic(ReceptionRequest request, ReceptionDynamicContext context) {
        List<RawLog> rawLogs = request.getRawLog();
        ReceiverEndpoint endpoint = context.getReceiverEndpoint();
        log.info("日志接收责任链-日志格式与负载大小校验节点接管 request:{}", JSON.toJSONString(request));
        // 2. 预验证日志格式和大小
        List<RawLog> validLogs = new ArrayList<>();
        List<RawLog> invalidLogs = new ArrayList<>();
        for (RawLog rawLog : rawLogs) {
            // 1. 验证日志格式是否匹配端点配置
            if (endpoint.getFormat() != rawLog.getFormat()) {
                log.warn("接收日志失败: 日志格式不匹配, expected={}, actual={}",
                        endpoint.getFormat(), rawLog.getFormat());
                invalidLogs.add(rawLog);
                continue;
            }
            // 2. 验证负载大小是否超限
            int logSize = rawLog.getContent().getBytes().length;
            if (logSize > endpoint.getMaxPayloadSize()) {
                log.warn("接收日志失败: 日志大小超限, size={}, maxSize={}",
                        logSize, endpoint.getMaxPayloadSize());
                invalidLogs.add(rawLog);
                continue;
            }
            validLogs.add(rawLog);
        }
        // 如果所有日志都不合格，返回失败
        if (validLogs.isEmpty()) {
            log.warn("全部日志均无效，处理失败: request={}", JSON.toJSONString(request));
            return ReceptionResult.failure("所有日志均不合规");
        }

        context.setValidLogs(validLogs);
        context.setInvalidLogs(invalidLogs);
        log.info("日志接收责任链-日志格式与负载大小校验节点放行 request:{}", JSON.toJSONString(request));

        return next().logic(request, context);
    }

    @Override
    protected String ruleNode() {
        return DefaultLogicChainFactory.NodeType.RECEPTION_FORMAT_SIZE_NODE.getCode();
    }

}
