package cn.cug.sxy.types.framework.chain.assembler;

import cn.cug.sxy.types.framework.chain.ILogicChain;

/**
 * @version 1.0
 * @Date 2025/7/8 08:44
 * @Description 责任链组装接口
 * @Author jerryhotton
 */

public interface ILogicChainAssembler<T, R, D> {

    ILogicChain<T, R, D> assembler();

}
