package cn.cug.sxy.domain.reception.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @version 1.0
 * @Date 2025/7/7 14:50
 * @Description 端点状态枚举
 * @Author jerryhotton
 */

@Getter
@AllArgsConstructor
public enum EndpointStatus {

    ACTIVE(0, "活跃"),
    INACTIVE(1, "不活跃"),
    OVERLOADED(2, "过载"),
    ERROR(3, "错误");

    public static EndpointStatus valueOf(Integer code) {
        switch (code) {
            case 0:
                return ACTIVE;
            case 1:
                return INACTIVE;
            case 2:
                return OVERLOADED;
            case 3:
                return ERROR;
            default:
                throw new IllegalArgumentException("Invalid EndpointStatus code: " + code);
        }
    }

    private final Integer code;
    private final String info;

}
