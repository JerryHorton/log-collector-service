package cn.cug.sxy.domain.reception.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @version 1.0
 * @Date 2025/7/7 11:50
 * @Description 日志格式枚举
 * @Author jerryhotton
 */

@Getter
@AllArgsConstructor
public enum LogFormat {

    JSON(0, "JSON格式"),
    TEXT(1, "文本格式"),
    XML(2, "XML格式"),
    BINARY(3, "二进制格式"),
    CUSTOM(4, "自定义格式");

    public static LogFormat valueOf(Integer code) {
        switch (code) {
            case 0:
                return JSON;
            case 1:
                return TEXT;
            case 2:
                return XML;
            case 3:
                return BINARY;
            case 4:
                return CUSTOM;
            default:
                throw new IllegalArgumentException("Invalid LogFormat code: " + code);
        }
    }

    private final Integer code;
    private final String info;

}
