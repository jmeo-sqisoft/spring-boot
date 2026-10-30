package com.stagora.global.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * app.cors.allowed-origin-patterns에 지정한 출처의 교차 출처 요청을 허용한다.
 * 기본 허용 출처는 AppProperties의 로컬 개발 환경 설정을 사용한다.
 */
@Configuration
public class CorsConfig {
    /**
     * 모든 경로에 자격 증명을 허용하는 CORS 필터를 등록한다.
     * 요청 헤더는 모두 허용하고, 응답의 Authorization 헤더를 클라이언트에 노출한다.
     */
    @Bean
    public CorsFilter corsFilter(AppProperties appProperties) {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(appProperties.getCors().getAllowedOriginPatterns());
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setExposedHeaders(List.of("Authorization"));
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
