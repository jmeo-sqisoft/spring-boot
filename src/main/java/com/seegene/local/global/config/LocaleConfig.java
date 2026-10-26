package com.seegene.local.global.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * 다국어(ko-KR, en-US) MessageSource 설정.
 */
@Configuration
public class LocaleConfig {

	@Bean
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasenames("classpath:messages/messages");
		messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
		messageSource.setDefaultLocale(Locale.KOREA);
		return messageSource;
	}
}
