package com.kipperdev.orderhub.config;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class FeignConfig {

    @Value("${feign.client.connect-timeout:5000}")
    private int connectTimeout;

    @Value("${feign.client.read-timeout:10000}")
    private int readTimeout;

    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
            connectTimeout, TimeUnit.MILLISECONDS,
            readTimeout, TimeUnit.MILLISECONDS,
            true
        );
    }

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(
            100L,    // period
            1000L,   // maxPeriod
            3        // maxAttempts
        );
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    public static class CustomErrorDecoder implements ErrorDecoder {
        private final ErrorDecoder defaultErrorDecoder = new Default();

        @Override
        public Exception decode(String methodKey, feign.Response response) {
            log.error("Erro na chamada Feign - método: {}, status: {}, motivo: {}", 
                methodKey, response.status(), response.reason());
            
            switch (response.status()) {
                case 400:
                    return new IllegalArgumentException("Requisição inválida: " + response.reason());
                case 401:
                    return new SecurityException("Não autorizado: " + response.reason());
                case 404:
                    return new RuntimeException("Recurso não encontrado: " + response.reason());
                case 500:
                    return new RuntimeException("Erro interno do servidor: " + response.reason());
                case 503:
                    return new RuntimeException("Serviço indisponível: " + response.reason());
                default:
                    return defaultErrorDecoder.decode(methodKey, response);
            }
        }
    }
}