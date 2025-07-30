package com.kipperdev.orderhub.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@Slf4j
public class AbacatePayFeignConfig {

    @Value("${abacate.api.token:}")
    private String abacateApiToken;

    @Bean
    public RequestInterceptor abacatePayRequestInterceptor() {
        return new AbacatePayRequestInterceptor(abacateApiToken);
    }

    public static class AbacatePayRequestInterceptor implements RequestInterceptor {
        private final String apiToken;

        public AbacatePayRequestInterceptor(String apiToken) {
            this.apiToken = apiToken;
        }

        @Override
        public void apply(RequestTemplate template) {
            if (StringUtils.hasText(apiToken) && !"mock-token".equals(apiToken)) {
                template.header("Authorization", "Bearer " + apiToken);
                log.debug("Adicionado Bearer token para requisição Abacatepay");
            } else {
                log.warn("Token da API Abacatepay não configurado ou usando token mock");
            }
            
            template.header("Content-Type", "application/json");
            template.header("Accept", "application/json");
        }
    }
}