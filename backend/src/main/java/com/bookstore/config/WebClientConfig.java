package com.bookstore.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for WebClient beans used for reactive HTTP communication
 */
@Configuration
public class WebClientConfig {

    @Value("${external.book.service.url:http://localhost:8080}")
    private String bookServiceUrl;

    @Value("${external.author.service.url:http://localhost:8080}")
    private String authorServiceUrl;

    @Value("${webclient.timeout.connection:5000}")
    private int connectionTimeout;

    @Value("${webclient.timeout.read:10000}")
    private int readTimeout;

    @Value("${webclient.timeout.write:10000}")
    private int writeTimeout;

    /**
     * WebClient for book service communication
     */
    @Bean("bookServiceWebClient")
    public WebClient bookServiceWebClient() {
        return createWebClient(bookServiceUrl);
    }

    /**
     * WebClient for author service communication
     */
    @Bean("authorServiceWebClient")
    public WebClient authorServiceWebClient() {
        return createWebClient(authorServiceUrl);
    }

    /**
     * Generic WebClient for external API calls
     */
    @Bean("genericWebClient")
    public WebClient genericWebClient() {
        return createWebClient("");
    }

    /**
     * Creates a configured WebClient with timeout, retry, and error handling
     */
    private WebClient createWebClient(String baseUrl) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(writeTimeout, TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .filter(logRequest())
                .filter(logResponse())
                .filter(errorHandlingFilter())
                .build();
    }

    /**
     * Filter to log outgoing requests
     */
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            if (org.slf4j.LoggerFactory.getLogger(WebClientConfig.class).isDebugEnabled()) {
                org.slf4j.LoggerFactory.getLogger(WebClientConfig.class)
                        .debug("Request: {} {}", clientRequest.method(), clientRequest.url());
            }
            return Mono.just(clientRequest);
        });
    }

    /**
     * Filter to log incoming responses
     */
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (org.slf4j.LoggerFactory.getLogger(WebClientConfig.class).isDebugEnabled()) {
                org.slf4j.LoggerFactory.getLogger(WebClientConfig.class)
                        .debug("Response Status: {}", clientResponse.statusCode());
            }
            return Mono.just(clientResponse);
        });
    }

    /**
     * Filter for centralized error handling
     */
    private ExchangeFilterFunction errorHandlingFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().isError()) {
                return clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            org.slf4j.LoggerFactory.getLogger(WebClientConfig.class)
                                    .error("Error response: {} - {}", clientResponse.statusCode(), errorBody);
                            return Mono.just(clientResponse);
                        });
            }
            return Mono.just(clientResponse);
        });
    }
}