package com.stagora.global.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import com.navercorp.lucy.security.xss.servletfilter.XssEscapeFilter;
import com.stagora.global.filter.LucyXssFilter;

import jakarta.servlet.DispatcherType;

/**
 * 문자 인코딩 필터 이후에 모든 경로의 쿼리·폼 파라미터에 Lucy XSS 처리를 적용한다.
 */
@Configuration
public class XssConfig {
    @Bean
    public FilterRegistrationBean<LucyXssFilter> lucyXssFilter() {
        var registration = new FilterRegistrationBean<>(
            new LucyXssFilter(XssEscapeFilter.getInstance()));
        registration.setName("lucyXssFilter");
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.setAsyncSupported(true);
        return registration;
    }
}
