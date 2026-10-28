package com.stagora.common.api;

import com.stagora.common.constants.Language;
import com.stagora.common.constants.Messages;
import com.stagora.common.util.StringUtil;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Locale;

/**
 * API 컨트롤러에서 사용하는 공통 성공 응답과 언어 코드 처리.
 */
public abstract class AbstractApiController {

	/**
	 * 하위 컨트롤러에 주입된 MessageSource를 제공한다.
	 */
	protected abstract MessageSource getMessageSource();

	/**
	 * 요청 로케일에 맞는 메시지와 데이터를 SSP 형식으로 감싸 HTTP 200으로 반환한다.
	 */
	protected <T> ResponseEntity<SspResponse<T>> success(T data) {
		Locale locale = LocaleContextHolder.getLocale();
		String message = getMessageSource().getMessage(Messages.KEY_SUCCESS_DEFAULT, null, Messages.KEY_SUCCESS_DEFAULT, locale);
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(SspResponse.success(message, data));
	}

	/**
	 * 로케일의 언어 코드가 없으면 기본 언어 코드를 사용한다.
	 */
	protected String getLanguage(Locale locale) {
		String lang = locale == null ? "" : locale.getLanguage();
		if (StringUtil.isBlank(lang)) {
			lang = Language.DEFAULT;
		}
		return lang;
	}
}
