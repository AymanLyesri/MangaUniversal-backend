package com.mangareader.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * Configuration for WebClient with proper timeouts and settings
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        reactor.netty.http.client.HttpClient httpClient = reactor.netty.http.client.HttpClient.create()
                .responseTimeout(Duration.ofSeconds(30))
                .doOnConnected(conn -> 
                    conn.addHandlerLast(new io.netty.handler.timeout.ReadTimeoutHandler(30))
                        .addHandlerLast(new io.netty.handler.timeout.WriteTimeoutHandler(30))
                );

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
