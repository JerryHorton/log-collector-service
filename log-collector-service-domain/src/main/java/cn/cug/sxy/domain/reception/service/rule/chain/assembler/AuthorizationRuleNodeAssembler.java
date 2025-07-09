package cn.cug.sxy.domain.reception.service.rule.chain.assembler;

import cn.cug.sxy.domain.reception.model.entity.AuthorizationDynamicContext;
import cn.cug.sxy.domain.reception.model.valobj.AuthorizationRequest;
import cn.cug.sxy.domain.reception.model.valobj.AuthorizationResult;
import cn.cug.sxy.domain.reception.service.rule.chain.ILogicChain;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @version 1.0
 * @Date 2025/7/8 11:22
 * @Description 应用授权规则责任链装配器
 * @Author jerryhotton
 */

@Component(value = "authorization_rule")
public class AuthorizationRuleNodeAssembler extends AbstractLogicChainAssembler<AuthorizationRequest, AuthorizationResult, AuthorizationDynamicContext> {

    public AuthorizationRuleNodeAssembler(Map<String, ILogicChain<AuthorizationRequest, AuthorizationResult, AuthorizationDynamicContext>> ruleNodeMap) {
        super(ruleNodeMap);
    }

    @Override
    public ILogicChain<AuthorizationRequest, AuthorizationResult, AuthorizationDynamicContext> assembler() {
        ILogicChain<AuthorizationRequest, AuthorizationResult, AuthorizationDynamicContext> head = ruleNodeMap.get("authorization_basic_node");
        head.appendNext(ruleNodeMap.get("authorization_permission_node"));

        return head;
    }

}
