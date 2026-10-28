package com.stagora.common.api;

import com.stagora.common.constants.Example;
import com.stagora.common.constants.Messages;
import com.stagora.common.util.StringUtil;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Locale;

/**
 * 모든 API Controller의 공통 부모. 응답 래핑 등 공통 로직 제공.
 * <p>설계 기준: doc/public/spec/architecture/03-package-structure.md §3.1</p>
 * <p>다국어: doc/public/spec/architecture/04-i18n-and-exception.md §4, success 메시지는 MessageSource로 해석.</p>
 * <p>MessageSource는 하위 클래스에서 생성자 주입 후 {@link #getMessageSource()}로 제공한다.</p>
 */
public abstract class AbstractApiController {

	/**
	 * 하위 Controller는 {@code private final MessageSource} 를 생성자 주입하고 이 메서드를 오버라이드한다.
	 */
	protected abstract MessageSource getMessageSource();

	/**
	 * SSP 형식으로 성공 응답 반환.
	 * <p>메시지는 Accept-Language에 따라 MessageSource로 다국어 해석.</p>
	 *
	 * @param data 응답 데이터
	 * @return ResponseEntity with SspResponse (code S0000)
	 */
	protected <T> ResponseEntity<SspResponse<T>> success(T data) {
		Locale locale = LocaleContextHolder.getLocale();
		String message = getMessageSource().getMessage(Messages.KEY_SUCCESS_DEFAULT, null, Messages.KEY_SUCCESS_DEFAULT, locale);
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(SspResponse.success(message, data));
	}

	protected String getLanguage(Locale locale) {
		String lang = locale == null ? "" : locale.getLanguage();
		if (StringUtil.isBlank(lang)) {
			lang = Example.LANG;
		}
		return lang;
	}
}
