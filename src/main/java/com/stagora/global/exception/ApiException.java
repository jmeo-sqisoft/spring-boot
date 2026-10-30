package com.stagora.global.exception;

import com.stagora.global.api.ApiResponseCode;

import lombok.Getter;

/**
 * API 오류의 HTTP 상태, 응답 코드, 다국어 메시지 키와 부가 데이터를 전달한다.
 * 메시지 키를 예외 메시지로 보관하며, 실제 메시지 조회와 HTTP 응답 생성은 수행하지 않는다.
 */
@Getter
public class ApiException extends RuntimeException {
    private final int statusCode;
    private final String errorCode;
    private final String messageKey;
    @SuppressWarnings("SerializableClass")
    private final transient Object data;

    /**
     * HTTP 상태, 응답 코드와 메시지 키를 직접 지정한다.
     */
    public ApiException(int statusCode, String errorCode, String messageKey) {
        this(statusCode, errorCode, messageKey, null);
    }

    /**
     * 오류 정보와 함께 전달할 부가 데이터를 지정한다.
     */
    public ApiException(int statusCode, String errorCode, String messageKey, Object data) {
        super(messageKey);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.messageKey = messageKey;
        this.data = data;
    }

    /**
     * 오류 정보와 원인 예외를 지정한다.
     */
    public ApiException(int statusCode, String errorCode, String messageKey, Throwable cause) {
        this(statusCode, errorCode, messageKey, null, cause);
    }

    /**
     * 오류 정보, 부가 데이터와 원인 예외를 지정한다.
     */
    public ApiException(int statusCode, String errorCode, String messageKey,
        Object data, Throwable cause) {
        super(messageKey, cause);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.messageKey = messageKey;
        this.data = data;
    }

    /**
     * 응답 코드에 정의된 HTTP 상태와 기본 메시지 키를 사용한다.
     */
    public ApiException(ApiResponseCode responseCode) {
        this(responseCode, (String) null);
    }

    /**
     * 응답 코드의 메시지 키를 재지정한다. null이면 기본 메시지 키를 사용한다.
     */
    public ApiException(ApiResponseCode responseCode, String messageKeyOverride) {
        this(responseCode, messageKeyOverride, null);
    }

    /**
     * 응답 코드, 선택적인 메시지 키와 부가 데이터를 지정한다.
     */
    public ApiException(ApiResponseCode responseCode, String messageKeyOverride, Object data) {
        super(messageKeyOverride != null
            ? messageKeyOverride : responseCode.getDefaultMessageKey());
        this.statusCode = responseCode.getStatusCode();
        this.errorCode = responseCode.getCode();
        this.messageKey = messageKeyOverride != null
            ? messageKeyOverride : responseCode.getDefaultMessageKey();
        this.data = data;
    }

    /**
     * 응답 코드의 기본 오류 정보와 원인 예외를 지정한다.
     */
    public ApiException(ApiResponseCode responseCode, Throwable cause) {
        this(responseCode, null, null, cause);
    }

    /**
     * 응답 코드, 선택적인 메시지 키, 부가 데이터와 원인 예외를 지정한다.
     */
    public ApiException(ApiResponseCode responseCode, String messageKeyOverride,
        Object data, Throwable cause) {
        super(messageKeyOverride != null
            ? messageKeyOverride : responseCode.getDefaultMessageKey(), cause);
        this.statusCode = responseCode.getStatusCode();
        this.errorCode = responseCode.getCode();
        this.messageKey = messageKeyOverride != null
            ? messageKeyOverride : responseCode.getDefaultMessageKey();
        this.data = data;
    }
}
