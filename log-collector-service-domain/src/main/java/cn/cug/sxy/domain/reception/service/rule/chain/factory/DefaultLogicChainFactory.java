package cn.cug.sxy.domain.reception.service.rule.chain.factory;

import cn.cug.sxy.domain.reception.service.rule.chain.ILogicChain;
import cn.cug.sxy.domain.reception.service.rule.chain.assembler.ILogicChainAssembler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @version 1.0
 * @Date 2025/7/7 14:07
 * @Description 默认责任链工厂
 * @Author jerryhotton
 */

@Component
public class DefaultLogicChainFactory {

    private final Map<String, ILogicChainAssembler<?, ?, ?>> assemblerMap;

    public DefaultLogicChainFactory(Map<String, ILogicChainAssembler<?, ?, ?>> assemblerMap) {
        this.assemblerMap = assemblerMap;
    }

    public ILogicChain<?, ?, ?> openLogicChain(String ruleName) {
        return assemblerMap.get(ruleName).assembler();
    }

    @Getter
    @AllArgsConstructor
    public enum NodeType {

        RECEPTION_BASIC_NODE("reception_basic_node", "日志接收基础规则校验节点"),
        RECEPTION_FORMAT_SIZE_NODE("reception_format_size_node", "日志格式与负载大小校验节点"),
        RECEPTION_AUTH_LIMIT_NODE("reception_auth_limit_node", "应用授权与限流校验节点"),
        RECEPTION_SINGLETON_DEFAULT_NODE("reception_singleton_default_node", "默认节点（单条接收）"),
        RECEPTION_BATCH_DEFAULT_NODE("reception_batch_default_node", "默认节点（批次接收）"),

        AUTHORIZATION_BASIC_NODE("authorization_basic_node", "权限基础校验节点"),
        AUTHORIZATION_PERMISSION_NODE("authorization_permission_node", "IP及端点访问权限校验节点"),

        ;

        private final String code;
        private final String info;

    }

    @Getter
    @AllArgsConstructor
    public enum RuleType {

        RECEPTION_SINGLETON_RULE("reception_singleton_rule", "日志接收规则（单条）"),
        RECEPTION_BATCH_RULE("reception_batch_rule", "日志接收规则（批次）"),
        AUTHORIZATION_RULE("authorization_rule", "应用授权规则");

        private final String code;
        private final String info;

    }


}
