package cn.cug.sxy.api;

import cn.cug.sxy.api.dto.*;
import cn.cug.sxy.api.response.Response;
import jakarta.servlet.http.HttpServletRequest;


/**
 * @version 1.0
 * @Date 2025/7/10 17:12
 * @Description 日志接收服务接口
 * @Author jerryhotton
 */

public interface ILogCollectorService {

    /**
     * 接收单条日志
     * 接收并处理单条日志数据
     *
     * @param request HTTP请求
     * @param requestDTO 日志接收请求
     * @return 接收结果
     */
    Response<LogReceiveResponseDTO> receiveLog(HttpServletRequest request, LogReceiveRequestDTO requestDTO);

    /**
     * 批量接收日志
     * 批量接收并处理多条日志数据
     *
     * @param requestDTO 批量日志请求
     * @return 接收结果
     */
    Response<BatchLogReceiveResponseDTO> receiveBatchLogs(BatchLogReceiveRequestDTO requestDTO);

    /**
     * 查询日志
     * 根据查询条件查询日志
     *
     * @param requestDTO 日志查询请求
     * @return 日志查询响应
     */
    Response<LogQueryResponseDTO> queryLogs(LogQueryRequestDTO requestDTO);

}
