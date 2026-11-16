package com.stagora.reports.result.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DB에 저장된 단일 페이지 PNG 원본 조회 모델")
public record PatientPageContent(
    @Schema(description = "페이지 번호", example = "1")
    int page,
    @Schema(description = "comm_files.contents의 PNG bytea 원본. JSON 응답에서는 Base64로 전달")
    byte[] contents
) {
}
