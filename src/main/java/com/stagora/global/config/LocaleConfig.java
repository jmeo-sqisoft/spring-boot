package com.stagora.global.config;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * 다국어 응답 메시지를 읽는 MessageSource를 구성한다.
 */
@Configuration
public class LocaleConfig {
    /**
     * messages/messages 번들을 UTF-8로 읽고 기본 로케일을 한국어로 지정한다.
     * 조회용 언어 코드의 기본값은 별도로 LocaleUtil과 Language에서 관리한다.
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource =
            new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames("classpath:messages/messages");
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setDefaultLocale(Locale.KOREA);
        return messageSource;
    }
}
