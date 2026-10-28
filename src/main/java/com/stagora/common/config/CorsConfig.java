package com.stagora.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * CORS 필터. app.cors.allowed-origin-patterns 설정 사용.
 * <p>로컬 개발 시 프론트엔드(localhost 등)에서 API 호출 허용.</p>
 * <p>허용 Origin 패턴은 app.cors.allowed-origin-patterns로 설정 가능.</p>
 */
@Configuration
public class CorsConfig {

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
