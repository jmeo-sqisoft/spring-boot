package com.seegene.local.base.dto.response;

import com.seegene.local.base.enums.ApiResponseCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * SSP 기본 응답 구조 (code, message, data).
 * <p>설계 기준: doc/public/spec/contracts/endpoints-v1.md §2.1</p>
 */
@Builder
@Schema(description = "SSP 기본 응답 (code, message, data)")
public record SspResponse<T>(
	@Schema(description = "결과 코드", example = "S0000")
	String code,
	@Schema(description = "결과 메시지")
	String message,
	@Schema(description = "응답 데이터")
	T data
) {
	public static final String CODE_SUCCESS = ApiResponseCode.S0000.getCode();

	@SuppressWarnings("unchecked")
	public static <T> SspResponse<T> success(String message, Object data) {
		return (SspResponse<T>) SspResponse.builder()
			.code(CODE_SUCCESS)
			.message(message)
			.data(data)
			.build();
	}
}
