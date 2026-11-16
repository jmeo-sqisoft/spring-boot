package com.stagora.reports.result.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 미리보기 팝업에서 사용할 목록과 동일한 검사 정보 및 전체 페이지 이미지이다.
 */
@Schema(description = "리포트 단건 정보와 전체 PNG 이미지")
public record PatientDetailResponse(
    @Schema(description = "목록 조회와 동일한 검사 정보·결과 요약·출력 이력")
    PatientSearchResponse report,
    @Schema(description = "페이지 번호 오름차순의 PNG 목록. 각 contents는 Base64 문자열로 반환")
    List<PatientPageImageResponse> pages
) {
}
