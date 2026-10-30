package com.stagora.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * OpenAPI 문서의 제목, 설명, 버전을 정의한다.
 * 문서 경로와 활성화 여부는 springdoc 설정을 따른다.
 */
@Configuration
public class OpenApiConfig {
    /**
     * 애플리케이션의 기본 API 문서 정보를 등록한다.
     */
    @Bean
    public OpenAPI localOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Local API")
                .description("로컬 프로젝트 API 문서")
                .version("v0.0.1"));
    }
}
