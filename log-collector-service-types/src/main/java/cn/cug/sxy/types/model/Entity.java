package cn.cug.sxy.types.model;

/**
 * @version 1.0
 * @Date 2025/7/7 11:01
 * @Description 实体接口（具有唯一标识的领域对象）
 * @Author jerryhotton
 */

public interface Entity<ID extends Identifier> {

    /**
     * 获取实体ID
     * @return 实体的唯一标识
     */
    ID getId();

    /**
     * 实体相等性比较，基于ID
     */
    @Override
    boolean equals(Object obj);

    /**
     * 实体哈希码，基于ID
     */
    @Override
    int hashCode();

}
