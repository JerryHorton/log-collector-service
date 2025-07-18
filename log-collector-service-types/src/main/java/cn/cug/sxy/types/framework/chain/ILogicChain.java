package cn.cug.sxy.types.framework.chain;

/**
 * @version 1.0
 * @Date 2025/2/24 13:48
 * @Description 责任链接口
 * @Author jerryhotton
 */

public interface ILogicChain<T, R, D> extends ILogicChainHandler<T, R, D>  {

    R logic(T requestParameter, D dynamicContext);

}
