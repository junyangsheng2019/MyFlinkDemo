package com.example.es.esDemo;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import java.io.IOException;

/**
 * @description: new class
 * @author: shengjunyang
 * @date:2020/9/27 8:34 下午
 */
@Slf4j
public class EsClientBuilders {


    String esUrl ="127.0.0.1";
    int esPort = 9200;

    private String schema = "http";
    // 设置连接超时时间
    private int connectTimeOut = 2000;
    private int socketTimeOut = 30000;
    private int connectionRequestTimeOut = 10000;
    // 一次最多接收请求
    private int maxConnectNum = 100;
    // 某一个服务每次能并行接收的请求数量
    private int maxConnectPerRoute = 100;
    private boolean uniqueConnectTimeConfig = true;
    private boolean uniqueConnectNumConfig = true;
    private HttpHost httpHost;
    private RestClientBuilder builder;
    private RestHighLevelClient client;
    /**
     * 初始化连接
     * @return
     */
    @Bean(name = "restHighLevelClient")
    public RestHighLevelClient getClientBuilder() {
        httpHost = new HttpHost(esUrl, esPort, schema);
        builder = RestClient.builder(httpHost);
        // 设置连接时间
        if (uniqueConnectTimeConfig) {
            setConnnectTimeOutConfig();
        }
        // 设置连接数
        if (uniqueConnectNumConfig) {
            setMutiConnectConfig();
        }

        client = new RestHighLevelClient(builder);
        return client;
    }

    /**
     * 异步httpclient的连接延迟配置
     * 设置修改默认请求配置的回调（例如：请求超时，认证，或者其他设置
     */
    public void setConnnectTimeOutConfig() {
        builder.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
            @Override
            public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder builder) {
                builder.setConnectTimeout(connectTimeOut);
                builder.setSocketTimeout(socketTimeOut);
                builder.setConnectionRequestTimeout(connectionRequestTimeOut);
                return builder;
            }
        });
    }

    /**
     * 线程设置
     */
    public void setMutiConnectConfig() {
        builder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
            @Override
            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
                httpAsyncClientBuilder.setMaxConnTotal(maxConnectNum);
                httpAsyncClientBuilder.setMaxConnPerRoute(maxConnectPerRoute);
                return httpAsyncClientBuilder;
            }
        });
    }

    /**
     * 关闭es连接
     */
    public void close() {
        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {
                log.error("关闭es连接异常");
            }
        }
    }


}
