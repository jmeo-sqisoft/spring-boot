package com.stagora.global.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import com.navercorp.lucy.security.xss.servletfilter.XssEscapeFilter;
import com.stagora.global.filter.LucyXssFilter;

import jakarta.servlet.DispatcherType;

/**
 * Lucy 규칙 엔진과 사용자 정의 Jakarta 필터를 등록한다.
 * 문자 인코딩 필터 이후에 모든 경로의 쿼리·폼 파라미터를 처리한다.
 * app.xss.enabled가 true일 때만 필터를 등록한다. 기본값은 비활성화이다.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "app.xss", name = "enabled", havingValue = "true")
public class XssConfig {
    /**
     * 클래스패스의 lucy-xss-servlet-filter-rule.xml을 사용하는 규칙 엔진을 제공한다.
     */
    @Bean
    public XssEscapeFilter lucyXssEscapeFilter() {
        return XssEscapeFilter.getInstance();
    }

    /**
     * Spring Boot가 사용자 정의 필터를 Servlet 컨테이너에 한 번 등록하도록 구성한다.
     */
    @Bean
    public FilterRegistrationBean<LucyXssFilter> lucyXssFilter(
        XssEscapeFilter lucyXssEscapeFilter) {
        var registration = new FilterRegistrationBean<>(
            new LucyXssFilter(lucyXssEscapeFilter));
        registration.setName("lucyXssFilter");
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.setAsyncSupported(true);
        return registration;
    }
}
