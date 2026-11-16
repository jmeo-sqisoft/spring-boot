package com.stagora.reports.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * JSON 응답에서 byte[]는 Base64 문자열로 직렬화되어 React에 전달된다.
 */
@Schema(description = "페이지 번호와 Base64 PNG 이미지")
public record PatientPageImageResponse(
    @Schema(description = "페이지 번호", example = "1")
    int page,
    @Schema(description = "이미지 형식", example = "image/png")
    String contentType,
    @Schema(description = "PNG 원본의 Base64 문자열", type = "string", format = "byte")
    byte[] contents
) {
}
