package cn.cug.sxy.domain.reception.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @version 1.0
 * @Date 2025/7/7 14:47
 * @Description 端点类型枚举
 * @Author jerryhotton
 */

@Getter
@AllArgsConstructor
public enum EndpointType {

    HTTP(0, "HTTP接口"),
    SDK(1, "SDK接入"),
    AGENT(2, "Agent采集"),
    TCP(3, "TCP套接字"),
    UDP(4, "UDP套接字"),

    ;

    public static EndpointType valueOf(Integer code) {
        switch (code) {
            case 0:
                return HTTP;
            case 1:
                return SDK;
            case 2:
                return AGENT;
            case 3:
                return TCP;
            case 4:
                return UDP;
            default:
                throw new IllegalArgumentException("Invalid EndpointType code: " + code);
        }
    }

    private final Integer code;
    private final String info;

}
