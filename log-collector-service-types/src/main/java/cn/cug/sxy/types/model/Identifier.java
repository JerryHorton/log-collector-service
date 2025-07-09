package cn.cug.sxy.types.model;

/**
 * @version 1.0
 * @Date 2025/7/7 11:00
 * @Description 标识符接口（领域实体的唯一标识）
 * @Author jerryhotton
 */

public interface Identifier {

    /**
     * 获取标识符的字符串表示
     * @return 标识符的字符串表示
     */
    String getValue();

}
