package com.stagora.global.exception;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.stagora.global.api.StaResponse;

import lombok.RequiredArgsConstructor;

/**
 * API 예외를 지정된 HTTP 상태와 다국어 메시지를 포함하는 공통 오류 응답으로 변환한다.
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class ApiExceptionHandler {
    private final MessageSource messageSource;

    /**
     * 메시지를 찾을 수 없으면 메시지 키를 사용하고 예외의 부가 데이터를 함께 반환한다.
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<StaResponse<Object>> handle(ApiException exception, Locale locale) {
        String message = messageSource.getMessage(exception.getMessageKey(), null,
            exception.getMessageKey(), locale);
        return ResponseEntity.status(exception.getStatusCode())
            .body(new StaResponse<>(exception.getErrorCode(), message, exception.getData()));
    }
}
