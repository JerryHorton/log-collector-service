package cn.cug.sxy.domain.reception.alc;

import cn.cug.sxy.domain.preprocess.service.ILogPreprocessService;
import cn.cug.sxy.domain.reception.model.entity.LogBatchEntity;
import cn.cug.sxy.domain.auth.model.valobj.AppId;
import cn.cug.sxy.domain.reception.model.valobj.EndpointId;
import cn.cug.sxy.domain.reception.model.valobj.ProcessedLog;
import cn.cug.sxy.domain.reception.model.valobj.RawLog;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @version 1.0
 * @Date 2025/7/9 13:51
 * @Description 预处理领域防腐层实现
 * @Author jerryhotton
 */

@Component
public class PreprocessGateway implements IPreprocessGateway {

    private final ILogPreprocessService logPreprocessService;

    public PreprocessGateway(ILogPreprocessService logPreprocessService) {
        this.logPreprocessService = logPreprocessService;
    }

    @Override
    public ProcessedLog preprocess(RawLog rawLog, AppId appId, EndpointId endpointId) {
        return null;
    }

    @Override
    public List<ProcessedLog> preprocessBatch(List<RawLog> rawLogs, AppId appId, EndpointId endpointId) {
        return List.of();
    }

    @Override
    public List<ProcessedLog> preprocessFromBatch(LogBatchEntity batch) {
        return logPreprocessService.preprocessFromBatch(batch);
    }
}
