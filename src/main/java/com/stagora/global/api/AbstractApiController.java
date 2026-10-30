package com.stagora.global.api;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.stagora.global.constants.Language;
import com.stagora.global.constants.Messages;
import com.stagora.global.util.StringUtil;

/**
 * API 컨트롤러의 공통 성공 응답 생성과 기본 언어 처리를 제공한다.
 */
public abstract class AbstractApiController {
    /**
     * 응답 메시지 조회에 사용할 하위 컨트롤러의 MessageSource를 반환한다.
     */
    protected abstract MessageSource getMessageSource();

    /**
     * 현재 요청 로케일의 성공 메시지와 데이터를 공통 응답으로 감싸 HTTP 200으로 반환한다.
     * 메시지를 찾지 못하면 메시지 키를 그대로 사용한다.
     */
    protected <T> ResponseEntity<StaResponse<T>> success(T data) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = getMessageSource().getMessage(
            Messages.KEY_SUCCESS_DEFAULT, null, Messages.KEY_SUCCESS_DEFAULT, locale);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(StaResponse.success(message, data));
    }

    /**
     * 로케일의 언어 코드를 반환한다. 로케일이 없거나 언어 코드가 비어 있으면 영어를 사용한다.
     */
    protected String getLanguage(Locale locale) {
        String lang = locale == null ? "" : locale.getLanguage();
        if (StringUtil.isBlank(lang)) {
            lang = Language.DEFAULT;
        }
        return lang;
    }
}
