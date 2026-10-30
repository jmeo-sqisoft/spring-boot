package com.stagora.global.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * 결과 코드, 메시지, 데이터를 담는 공통 API 응답.
 */
@Builder
@Schema(description = "STA 기본 응답 (code, message, data)")
public record StaResponse<T>(
	@Schema(description = "결과 코드", example = "S0000")
	String code,
	@Schema(description = "결과 메시지")
	String message,
	@Schema(description = "응답 데이터")
	T data
) {
	public static final String CODE_SUCCESS = ApiResponseCode.S0000.getCode();

	@SuppressWarnings("unchecked")
	public static <T> StaResponse<T> success(String message, Object data) {
		return (StaResponse<T>) StaResponse.builder()
			.code(CODE_SUCCESS)
			.message(message)
			.data(data)
			.build();
	}
}
