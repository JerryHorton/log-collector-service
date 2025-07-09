package cn.cug.sxy.domain.reception.service.rule.chain.assembler;

import cn.cug.sxy.domain.reception.service.rule.chain.ILogicChain;

import java.util.Map;

/**
 * @version 1.0
 * @Date 2025/7/8 08:35
 * @Description 责任链装配器抽象类
 * @Author jerryhotton
 */

public abstract class AbstractLogicChainAssembler<T, R, D> implements ILogicChainAssembler<T, R, D> {

    protected final Map<String, ILogicChain<T, R, D>> ruleNodeMap;

    public AbstractLogicChainAssembler(Map<String, ILogicChain<T, R, D>> ruleNodeMap) {
        this.ruleNodeMap = ruleNodeMap;
    }

    public abstract ILogicChain<T, R, D> assembler();

}
