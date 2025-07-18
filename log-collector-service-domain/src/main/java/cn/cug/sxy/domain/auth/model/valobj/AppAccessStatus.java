package cn.cug.sxy.domain.auth.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @version 1.0
 * @Date 2025/7/7 15:37
 * @Description 应用接入状态枚举
 * @Author jerryhotton
 */

@Getter
@AllArgsConstructor
public enum AppAccessStatus {

    ACTIVE(0, "应用接入处于活跃状态，可以正常访问"),
    INACTIVE(1, "应用接入处于不活跃状态，暂时无法访问"),
    BLOCKED(2, "应用接入被阻止，无法访问，通常是由于安全原因"),
    EXPIRED(3, "应用接入已过期，需要重新申请");

    private final Integer code;
    private final String info;

    /**
     * 检查状态是否允许访问
     *
     * @return 是否允许访问
     */
    public boolean isAccessAllowed() {
        return this == ACTIVE;
    }

    public static AppAccessStatus valueOf(Integer code) {
        switch (code) {
            case 0:
                return ACTIVE;
            case 1:
                return INACTIVE;
            case 2:
                return BLOCKED;
            case 3:
                return EXPIRED;
            default:
                throw new IllegalArgumentException("Invalid AppAccessStatus code: " + code);
        }
    }

}
