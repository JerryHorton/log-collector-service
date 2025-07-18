package cn.cug.sxy.trigger.http;

import cn.cug.sxy.api.ILogCollectorService;
import cn.cug.sxy.api.common.ApiConstants;
import cn.cug.sxy.api.dto.*;
import cn.cug.sxy.api.response.Response;
import cn.cug.sxy.domain.reception.model.valobj.LogFormat;
import cn.cug.sxy.domain.reception.model.valobj.RawLog;
import cn.cug.sxy.domain.reception.model.valobj.ReceptionResult;
import cn.cug.sxy.domain.reception.service.ILogReceptionService;
import cn.cug.sxy.domain.storage.model.valobj.LogQuery;
import cn.cug.sxy.domain.storage.model.valobj.LogQueryResult;
import cn.cug.sxy.domain.storage.service.ILogQueryService;
import cn.cug.sxy.types.enums.ResponseCode;
import cn.cug.sxy.types.exception.AppException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @version 1.0
 * @Date 2025/7/10 17:20
 * @Description
 * @Author jerryhotton
 */

@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/log/collector/")
public class LogCollectorController implements ILogCollectorService {

    private final ILogReceptionService logReceptionService;
    private final ILogQueryService logQueryService;

    public LogCollectorController(
            ILogReceptionService logReceptionService,
            ILogQueryService logQueryService) {
        this.logReceptionService = logReceptionService;
        this.logQueryService = logQueryService;
    }

    @RequestMapping(value = "receive_log", method = RequestMethod.POST)
    @Override
    public Response<LogReceiveResponseDTO> receiveLog(HttpServletRequest httpServletRequest, @RequestBody LogReceiveRequestDTO requestDTO) {
        try {
            String appId = requestDTO.getAppId();
            String endpointId = requestDTO.getEndpointId();
            log.info("接收单条日志开始 appId:{}, endpointId:{}", appId, endpointId);
            if (StringUtils.isEmpty(appId) || StringUtils.isEmpty(endpointId)) {
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER);
            }
            LogFormat logFormat = LogFormat.valueOf(requestDTO.getFormat());
            // 获取sourceId，如果为空则使用默认值
            String sourceId = requestDTO.getSourceId();
            if (StringUtils.isBlank(sourceId)) {
                // 默认使用appId作为sourceId
                sourceId = appId;
            }
            // 获取timestamp，如果为空则使用当前时间
            Instant timestamp;
            if (requestDTO.getTimestamp() != null) {
                timestamp = Instant.ofEpochMilli(requestDTO.getTimestamp());
            } else {
                timestamp = Instant.now();
            }
            // 创建原始日志对象
            RawLog rawLog = new RawLog(
                    requestDTO.getContent(),
                    sourceId,
                    logFormat,
                    timestamp,
                    buildMetadata(httpServletRequest, requestDTO)
            );
            // 调用领域服务处理日志
            ReceptionResult result = logReceptionService.receiveLog(rawLog, appId, endpointId);
            if (result.isBuffered()) {
                log.info("接收单条日志成功，已缓冲");
                // 已缓冲，返回响应
                return Response.<LogReceiveResponseDTO>builder()
                        .code(ResponseCode.SUCCESS.getCode())
                        .info(ResponseCode.SUCCESS.getInfo())
                        .data(new LogReceiveResponseDTO())
                        .build();
            } else {
                log.error("接收单条日志失败，已缓冲");

                return Response.<LogReceiveResponseDTO>builder()
                        .code(ResponseCode.SINGLE_LOG_RECEIVE_FAILED.getCode())
                        .info(ResponseCode.SINGLE_LOG_RECEIVE_FAILED.getInfo())
                        .build();
            }
        } catch (AppException e) {
            log.error("接收单条日志失败", e);

            return Response.<LogReceiveResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("接收单条日志异常", e);

            return Response.<LogReceiveResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "receive_batch_logs", method = RequestMethod.POST)
    @Override
    public Response<BatchLogReceiveResponseDTO> receiveBatchLogs(@RequestBody BatchLogReceiveRequestDTO requestDTO) {
        return null;
    }

    @RequestMapping(value = "query_logs", method = RequestMethod.POST)
    @Override
    public Response<LogQueryResponseDTO> queryLogs(@RequestBody LogQueryRequestDTO requestDTO) {
        try {
            log.info("查询日志开始 requestDTO:{}", requestDTO);
            // 构建查询条件
            LogQuery query = LogQuery.builder()
                    .startTime(requestDTO.getStartTime())
                    .endTime(requestDTO.getEndTime())
                    .appId(requestDTO.getAppId())
                    .level(requestDTO.getLevel())
                    .keyword(requestDTO.getKeyword())
                    .tags(requestDTO.getTags())
                    .pageNumber(requestDTO.getPageNumber())
                    .pageSize(requestDTO.getPageSize())
                    .ascending(requestDTO.isAscending())
                    .build();
            LogQueryResult result = logQueryService.queryLogs(query);
            log.info("日志接收成功 ");
            LogQueryResponseDTO data = LogQueryResponseDTO.builder()
                    .totalHits(result.getTotalHits())
                    .pageNumber(result.getPageNumber())
                    .pageSize(result.getPageSize())
                    .logs(result.getLogs().stream()
                            .map(log -> LogQueryResponseDTO.LogDocument.builder()
                                    .id(log.getId())
                                    .appId(log.getAppId())
                                    .endpointId(log.getEndpointId())
                                    .batchId(log.getBatchId())
                                    .level(log.getLevel())
                                    .content(log.getContent())
                                    .format(log.getFormat())
                                    .hostname(log.getHostname())
                                    .structuredData(log.getStructuredData())
                                    .tags(log.getTags())
                                    .timestamp(log.getTimestamp())
                                    .indexTime(log.getIndexTime())
                                    .source(log.getSource())
                                    .sourceId(log.getSourceId())
                                    .build()).collect(Collectors.toList()))
                    .build();

            return Response.<LogQueryResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(data)
                    .build();
        } catch (AppException e) {
            log.error("查询日志失败", e);

            return Response.<LogQueryResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("查询日志异常", e);

            return Response.<LogQueryResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 构建日志元数据
     */
    private Map<String, String> buildMetadata(HttpServletRequest request, LogReceiveRequestDTO requestDTO) {
        Map<String, String> metadata = new HashMap<>();
        if (StringUtils.isNoneBlank(requestDTO.getTags())) {
            metadata.put("tags", requestDTO.getTags());
        }
        if (StringUtils.isNoneBlank(requestDTO.getSource())) {
            metadata.put("source", requestDTO.getSource());
        }
        if (StringUtils.isNoneBlank(requestDTO.getHostname())) {
            metadata.put("hostname", requestDTO.getHostname());
        }
        if (StringUtils.isNoneBlank(requestDTO.getLevel())) {
            metadata.put("level", requestDTO.getLevel());
        }
        // 添加时间戳
        metadata.put("timestamp", String.valueOf(System.currentTimeMillis()));
        // 添加客户端IP
        String clientIp = (String) request.getAttribute(ApiConstants.HEADER_CLIENT_IP);
        metadata.put("clientIp", clientIp);
        // 添加原始日志结构化数据
        if (requestDTO.getStructuredData() != null) {
            for (Map.Entry<String, Object> entry : requestDTO.getStructuredData().entrySet()) {
                metadata.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }

        return metadata;
    }

}
