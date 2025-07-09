package cn.cug.sxy.domain.reception.service.rule.chain;

/**
 * @version 1.0
 * @Date 2025/2/24 13:59
 * @Description 责任链抽象类
 * @Author jerryhotton
 */

public abstract class AbstractLogicChainNode<T, R, D> implements ILogicChain<T, R, D> {

    private ILogicChain<T, R, D> next;

    @Override
    public ILogicChain<T, R, D> appendNext(ILogicChain<T, R, D> next) {
        this.next = next;
        return next;
    }

    @Override
    public ILogicChain<T, R, D> next() {
        return next;
    }

    protected abstract String ruleNode();

}
