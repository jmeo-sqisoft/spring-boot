package com.stagora.common.exception;

import com.stagora.common.api.ApiResponseCode;
import lombok.Getter;

/**
 * API 오류 시 사용하는 예외.
 * <p>설계 기준: doc/public/spec/architecture/04-i18n-and-exception.md §3</p>
 */
@Getter
public class ApiException extends RuntimeException {

	private final int statusCode;
	private final String errorCode;
	private final String messageKey;
	@SuppressWarnings("SerializableClass")
	private final transient Object data;

	public ApiException(int statusCode, String errorCode, String messageKey) {
		this(statusCode, errorCode, messageKey, null);
	}

	public ApiException(int statusCode, String errorCode, String messageKey, Object data) {
		super(messageKey);
		this.statusCode = statusCode;
		this.errorCode = errorCode;
		this.messageKey = messageKey;
		this.data = data;
	}

	public ApiException(int statusCode, String errorCode, String messageKey, Throwable cause) {
		this(statusCode, errorCode, messageKey, null, cause);
	}

	public ApiException(int statusCode, String errorCode, String messageKey, Object data, Throwable cause) {
		super(messageKey, cause);
		this.statusCode = statusCode;
		this.errorCode = errorCode;
		this.messageKey = messageKey;
		this.data = data;
	}

	/**
	 * ApiResponseCode enum 사용. messageKey는 enum의 defaultMessageKey 사용
	 */
	public ApiException(ApiResponseCode responseCode) {
		this(responseCode, (String) null);
	}

	/**
	 * ApiResponseCode enum 사용. messageKey 오버라이드 가능
	 */
	public ApiException(ApiResponseCode responseCode, String messageKeyOverride) {
		this(responseCode, messageKeyOverride, null);
	}

	/**
	 * ApiResponseCode enum 사용. messageKey 오버라이드, data 포함
	 */
	public ApiException(ApiResponseCode responseCode, String messageKeyOverride, Object data) {
		super(messageKeyOverride != null ? messageKeyOverride : responseCode.getDefaultMessageKey());
		this.statusCode = responseCode.getStatusCode();
		this.errorCode = responseCode.getCode();
		this.messageKey = messageKeyOverride != null ? messageKeyOverride : responseCode.getDefaultMessageKey();
		this.data = data;
	}

	/**
	 * ApiResponseCode enum 사용. cause 포함
	 */
	public ApiException(ApiResponseCode responseCode, Throwable cause) {
		this(responseCode, null, null, cause);
	}

	/**
	 * ApiResponseCode enum 사용. messageKey 오버라이드, data, cause 포함
	 */
	public ApiException(ApiResponseCode responseCode, String messageKeyOverride, Object data, Throwable cause) {
		super(messageKeyOverride != null ? messageKeyOverride : responseCode.getDefaultMessageKey(), cause);
		this.statusCode = responseCode.getStatusCode();
		this.errorCode = responseCode.getCode();
		this.messageKey = messageKeyOverride != null ? messageKeyOverride : responseCode.getDefaultMessageKey();
		this.data = data;
	}
}
