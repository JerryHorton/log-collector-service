package cn.cug.sxy.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @version 1.0
 * @Date 2025/7/11 15:01
 * @Description Elasticsearch配置
 * @Author jerryhotton
 */

@Configuration
@EnableConfigurationProperties(ElasticsearchConfigProperties.class)
@EnableScheduling
public class ElasticsearchConfig {

    @Bean
    public RestClient restClient(ElasticsearchConfigProperties properties) {
        return RestClient.builder(new HttpHost(properties.getHost(),
                properties.getPort(),
                properties.getProtocol())).build();
    }

    @Bean
    public ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());

        return new RestClientTransport(
                restClient,
                new JacksonJsonpMapper(objectMapper));
    }

    @Bean("elasticsearchClient")
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        return new ElasticsearchClient(transport);
    }

}
