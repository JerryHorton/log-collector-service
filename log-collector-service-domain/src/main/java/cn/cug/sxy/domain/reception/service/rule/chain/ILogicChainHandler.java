package cn.cug.sxy.domain.reception.service.rule.chain;

/**
 * @version 1.0
 * @Date 2025/2/24 15:42
 * @Description 装配
 * @Author jerryhotton
 */

public interface ILogicChainHandler<T, R, D> {

    ILogicChain<T, R, D> appendNext(ILogicChain<T, R, D> next);

    ILogicChain<T, R, D> next();

}
