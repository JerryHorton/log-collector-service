package cn.cug.sxy.domain.preprocess.service;

import cn.cug.sxy.domain.preprocess.service.strategy.IPreprocessStrategy;
import cn.cug.sxy.domain.preprocess.service.strategy.PreprocessStrategyFactory;
import cn.cug.sxy.domain.reception.model.entity.LogBatch;
import cn.cug.sxy.domain.reception.model.valobj.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @version 1.0
 * @Date 2025/7/9 13:55
 * @Description 日志预处理服务实现
 * @Author jerryhotton
 */

@Slf4j
@Service
public class LogPreprocessService implements ILogPreprocessService {

    private final PreprocessStrategyFactory strategyFactory;

    public LogPreprocessService(PreprocessStrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }

    @Override
    public ProcessedLog preprocess(RawLog rawLog, AppId appId, EndpointId endpointId) {
        try {
            // 1. 创建基础的处理后日志对象
            ProcessedLog processedLog = new ProcessedLog(
                    rawLog.getContent(),
                    rawLog.getFormat(),
                    rawLog.getMetadata(),
                    appId,
                    endpointId,
                    Instant.now()
            );
            // 2. 根据日志格式获取对应的预处理策略
            IPreprocessStrategy strategy = strategyFactory.getStrategy(rawLog.getFormat());
            // 3. 应用预处理策略
            if (strategy != null) {
                strategy.process(processedLog);
            }

            return processedLog;
        } catch (Exception e) {
            log.error("预处理日志失败: appId={}, endpointId={}, error={}",
                    appId.getValue(), endpointId.getValue(), e.getMessage(), e);
            // 创建一个标记为失败的处理后日志
            ProcessedLog failedLog = new ProcessedLog(
                    rawLog.getContent(),
                    rawLog.getFormat(),
                    rawLog.getMetadata(),
                    appId,
                    endpointId,
                    Instant.now()
            );
            failedLog.markValidationFailed("预处理失败: " + e.getMessage());
            return failedLog;
        }
    }

    @Override
    public List<ProcessedLog> preprocessBatch(List<RawLog> rawLogs, AppId appId, EndpointId endpointId) {
        // 按日志格式分组，以便批量处理相同格式的日志
        Map<LogFormat, List<RawLog>> logsByFormat = rawLogs.stream()
                .collect(Collectors.groupingBy(RawLog::getFormat));
        List<ProcessedLog> result = new ArrayList<>(rawLogs.size());
        // 对每种格式的日志批量处理
        for (Map.Entry<LogFormat, List<RawLog>> entry : logsByFormat.entrySet()) {
            LogFormat format = entry.getKey();
            List<RawLog> logs = entry.getValue();
            // 获取对应格式的预处理策略
            IPreprocessStrategy strategy = strategyFactory.getStrategy(format);
            if (strategy != null && strategy.supportsBatchProcessing()) {
                // 如果策略支持批量处理，则批量处理
                List<ProcessedLog> processedLogs = new ArrayList<>(logs.size());
                // 先创建基础的处理后日志对象
                for (RawLog rawLog : logs) {
                    processedLogs.add(new ProcessedLog(
                            rawLog.getContent(),
                            rawLog.getFormat(),
                            rawLog.getMetadata(),
                            appId,
                            endpointId,
                            Instant.now()
                    ));
                }
                // 批量应用预处理策略
                strategy.processBatch(processedLogs);
                result.addAll(processedLogs);
            } else {
                // 如果策略不支持批量处理，则逐个处理
                for (RawLog rawLog : logs) {
                    result.add(preprocess(rawLog, appId, endpointId));
                }
            }
        }

        return result;
    }

    @Override
    public List<ProcessedLog> preprocessFromBatch(LogBatch batch) {
        return preprocessBatch(batch.getLogs(), batch.getAppId(), batch.getEndpointId());
    }

}
