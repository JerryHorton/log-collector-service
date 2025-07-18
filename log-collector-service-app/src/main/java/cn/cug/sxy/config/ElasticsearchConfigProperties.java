package cn.cug.sxy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @version 1.0
 * @Date 2025/7/11 15:01
 * @Description Elasticsearch配置属性
 * @Author jerryhotton
 */

@Data
@ConfigurationProperties(prefix = "elasticsearch", ignoreInvalidFields = true)
public class ElasticsearchConfigProperties {

    private String host;

    private int port;

    private String protocol;

}
