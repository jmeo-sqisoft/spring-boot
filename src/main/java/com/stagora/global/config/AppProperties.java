package com.stagora.global.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * app 접두사의 애플리케이션 설정을 바인딩한다.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Cors cors = new Cors();

    /**
     * CORS 허용 출처 패턴을 관리한다. 기본값은 로컬 호스트와 각 포트의 요청을 허용한다.
     */
    @Getter
    @Setter
    public static class Cors {
        private List<String> allowedOriginPatterns = List.of(
            "http://localhost",
            "http://localhost:*",
            "http://127.0.0.1",
            "http://127.0.0.1:*",
            "http://[::1]",
            "http://[::1]:*");
    }
}
