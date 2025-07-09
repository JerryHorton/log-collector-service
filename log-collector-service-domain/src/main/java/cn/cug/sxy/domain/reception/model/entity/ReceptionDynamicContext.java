package cn.cug.sxy.domain.reception.model.entity;

import cn.cug.sxy.domain.reception.model.aggregate.ReceiverEndpoint;
import cn.cug.sxy.domain.reception.model.valobj.RawLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @version 1.0
 * @Date 2025/7/7 16:49
 * @Description 日志接收规则过滤中动态上下文
 * @Author jerryhotton
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReceptionDynamicContext {

    private ReceiverEndpoint receiverEndpoint;

    private List<RawLog> validLogs;

    private List<RawLog> invalidLogs;

}
