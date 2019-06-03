package com.freedom.config;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;


@Configuration
public class RestTemplateConfig {
    private Logger logger = LoggerFactory.getLogger(getClass());

    //连接超时（默认值 ：-1）
    @Value("${resttemplate.httpclient.requestconfig.connect-timeout:-1}")
    private Integer connectTimeout;

    //读超时（默认值 ：-1）
    @Value("${resttemplate.httpclient.requestconfig.read-timeout:-1}")
    private Integer readTimeout;

    //连接不够用的等待时间（默认值 ：-1）
    @Value("${resttemplate.httpclient.requestconfig.connection-request-timeout:-1}")
    private Integer connectionRequestTimeout;

    //最大连接数（默认值 ：10）
    @Value("${resttemplate.httpclient.pool.max-total:10}")
    private Integer maxTotal;

    //每个路由的最大连接数（默认值 ：5）
    @Value("${resttemplate.httpclient.pool.default-max-per-route:5}")
    private Integer defaultMaxPerRoute;


    @Bean("myRestTemplate")
    public RestTemplate restTemplate() {
        logger.info("开始配置restTemplate");
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());
        logger.info("restTemplate配置结束");
        return restTemplate;
    }

    @Bean
    public HttpComponentsClientHttpRequestFactory clientHttpRequestFactory() {
        try {
            HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

            //开始设置连接池
            PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
            poolingHttpClientConnectionManager.setMaxTotal(maxTotal);  //最大连接数，默认10
            poolingHttpClientConnectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);  //同路由并发数，默认5
            httpClientBuilder.setConnectionManager(poolingHttpClientConnectionManager);

            HttpClient httpClient = httpClientBuilder.build();
            // httpClient连接配置
            HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

            //连接超时
            if(connectTimeout > 0) {
                clientHttpRequestFactory.setConnectTimeout(connectTimeout);
                logger.info("配置restTemplate-connectTimeout: {}", connectTimeout);
            }
            //数据读取超时时间
            if(readTimeout > 0){
                clientHttpRequestFactory.setReadTimeout(readTimeout);
                logger.info("配置restTemplate-readTimeout: {}", readTimeout);
            }
            //连接不够用的等待时间
            if(connectionRequestTimeout > 0){
                clientHttpRequestFactory.setConnectionRequestTimeout(connectionRequestTimeout);
                logger.info("配置restTemplate-connectionRequestTimeout: {}", connectionRequestTimeout);
            }

            return clientHttpRequestFactory;
        }
        catch (Exception e) {
            logger.error("初始化clientHttpRequestFactory出错", e);
        }

        return null;
    }

}