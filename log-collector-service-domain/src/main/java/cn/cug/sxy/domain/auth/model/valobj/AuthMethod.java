package cn.cug.sxy.domain.auth.model.valobj;

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

    /**
     * HMAC签名认证
     */
    HMAC_SIGNATURE,

    /**
     * API密钥认证
     */
    API_KEY,

    /**
     * 基于IP的认证
     */
    IP_BASED,

    /**
     * 无认证（仅用于内部系统或测试）
     */
    NONE

}
