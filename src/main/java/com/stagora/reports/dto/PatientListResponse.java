package com.stagora.reports.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "필터 적용 후 전체 건수와 페이지별 환자 리포트 목록")
public record PatientListResponse(
    @Schema(description = "현재 검색 조건의 전체 리포트 건수", example = "5")
    long totalCount,
    @Schema(description = "현재 페이지 (1부터 시작)", example = "1")
    int page,
    @Schema(description = "페이지당 건수", example = "10")
    int size,
    @Schema(description = "전체 페이지 수. 조회 결과가 없으면 0", example = "1")
    long totalPages,
    @Schema(description = "Sample ID 검색 요약. 항상 반환하며 비활성 탭이면 모든 건수가 0",
        requiredMode = Schema.RequiredMode.REQUIRED)
    PatientSampleSummaryResponse sampleSummary,
    @Schema(description = "검사 분류 검색 요약. 항상 반환하며 비활성 탭이면 패널별 건수를 포함해 모두 0",
        requiredMode = Schema.RequiredMode.REQUIRED)
    PatientClassificationSummaryResponse classificationSummary,
    @Schema(description = "현재 페이지의 리포트. 미조회 Sample ID에 대한 빈 행은 추가하지 않음")
    List<PatientItemResponse> items
) {
}
