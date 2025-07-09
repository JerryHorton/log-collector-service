package cn.cug.sxy.domain.reception.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @version 1.0
 * @Date 2025/7/7 11:47
 * @Description 批次状态枚举
 * @Author jerryhotton
 */

@Getter
@AllArgsConstructor
public enum BatchStatus {

    PENDING(0, "待处理"),
    PROCESSING(1, "处理中"),
    PROCESSED(2, "已处理"),
    FAILED(3, "处理失败");


    private final Integer code;
    private final String info;

}
