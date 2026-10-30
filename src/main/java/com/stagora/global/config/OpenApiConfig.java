package com.stagora.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI localOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Local API")
                .description("로컬 프로젝트 API 문서")
                .version("v0.0.1"));
    }
}
