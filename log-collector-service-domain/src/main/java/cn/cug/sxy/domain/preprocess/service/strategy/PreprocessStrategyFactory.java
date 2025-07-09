package cn.cug.sxy.domain.preprocess.service.strategy;

import cn.cug.sxy.domain.reception.model.valobj.LogFormat;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version 1.0
 * @Date 2025/7/9 13:55
 * @Description 预处理策略工厂
 * @Author jerryhotton
 */

@Component
public class PreprocessStrategyFactory {

    private final List<IPreprocessStrategy> strategies;
    private final Map<LogFormat, IPreprocessStrategy> strategyMap = new HashMap<>();

    public PreprocessStrategyFactory(List<IPreprocessStrategy> strategies) {
        this.strategies = strategies;
    }

    @PostConstruct
    public void init() {
        // 初始化策略映射
        for (IPreprocessStrategy strategy : strategies) {
            strategyMap.put(strategy.getSupportedFormat(), strategy);
        }
    }

    /**
     * 获取指定日志格式的预处理策略
     *
     * @param format 日志格式
     * @return 预处理策略，如果不存在则返回null
     */
    public IPreprocessStrategy getStrategy(LogFormat format) {
        return strategyMap.get(format);
    }

    /**
     * 是否支持指定日志格式
     *
     * @param format 日志格式
     * @return 是否支持
     */
    public boolean supportsFormat(LogFormat format) {
        return strategyMap.containsKey(format);
    }
} 