package cn.cug.sxy.types.model;

/**
 * @version 1.0
 * @Date 2025/7/7 11:05
 * @Description 值对象接口（没有唯一标识的领域对象，通过其属性值确定相等性）
 * @Author jerryhotton
 */

public interface ValueObject {

    /**
     * 值对象相等性比较，基于所有属性
     */
    @Override
    boolean equals(Object obj);

    /**
     * 值对象哈希码，基于所有属性
     */
    @Override
    int hashCode();

}
