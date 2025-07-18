package cn.cug.sxy.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ResponseCode {

    SUCCESS("0000", "成功"),
    UN_ERROR("0001", "未知失败"),
    ILLEGAL_PARAMETER("0002", "非法参数"),

    INVALID_ACCESS_KEY("AUTH_001", "无效的访问密钥"),
    INVALID_APP_STATUS("AUTH_002", "应用状态无效"),
    APP_ACCESS_EXPIRED("AUTH_003", "应用访问已过期"),
    INVALID_TIMESTAMP("AUTH_004", "请求时间戳无效"),
    INVALID_TIMESTAMP_FORMAT("AUTH_005", "无效的时间戳格式"),
    SIGNATURE_VALIDATION_FAILED("AUTH_006", "签名验证失败"),

    SINGLE_LOG_RECEIVE_FAILED("RECV_001", "接收单条日志失败")

    ;

    private String code;
    private String info;

}
