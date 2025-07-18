package cn.cug.sxy.domain.reception.service.rule.chain.assembler;

import cn.cug.sxy.domain.reception.model.entity.ReceptionDynamicContext;
import cn.cug.sxy.domain.reception.model.valobj.ReceptionRequest;
import cn.cug.sxy.domain.reception.model.valobj.ReceptionResult;
import cn.cug.sxy.types.framework.chain.ILogicChain;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @version 1.0
 * @Date 2025/7/8 08:39
 * @Description 日志接收规则责任链装配器
 * @Author jerryhotton
 */

@Component("reception_singleton_rule")
public class ReceptionSingletonRuleNodeAssembler extends AbstractLogicChainAssembler<ReceptionRequest, ReceptionResult, ReceptionDynamicContext> {

    public ReceptionSingletonRuleNodeAssembler(Map<String, ILogicChain<ReceptionRequest, ReceptionResult, ReceptionDynamicContext>> ruleNodeMap) {
        super(ruleNodeMap);
    }

    @Override
    public ILogicChain<ReceptionRequest, ReceptionResult, ReceptionDynamicContext> assembler() {
        ILogicChain<ReceptionRequest, ReceptionResult, ReceptionDynamicContext> head = ruleNodeMap.get("reception_basic_node");
        head.appendNext(ruleNodeMap.get("reception_auth_limit_node"))
                .appendNext(ruleNodeMap.get("reception_format_size_node"))
                .appendNext(ruleNodeMap.get("reception_singleton_default_node"));

        return head;
    }

}
