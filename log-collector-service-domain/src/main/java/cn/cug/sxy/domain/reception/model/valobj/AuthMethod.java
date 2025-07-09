package cn.cug.sxy.domain.reception.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @version 1.0
 * @Date 2025/7/7 13:46
 * @Description 认证方法枚举
 * @Author jerryhotton
 */

@Getter
@AllArgsConstructor
public enum AuthMethod {

    ACCESS_KEY(0, "访问密钥认证"),
    TOKEN(1, "令牌认证"),
    CERTIFICATE(2, "证书认证"),
    BASIC_AUTH(3, "基本认证"),
    NONE(4, "无认证");

    private final Integer code;
    private final String info;

}
