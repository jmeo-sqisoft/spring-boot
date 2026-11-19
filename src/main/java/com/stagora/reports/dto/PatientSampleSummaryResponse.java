package com.stagora.reports.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Sample ID 검색 요약. 검사 분류 검색에서는 모든 건수가 0")
public record PatientSampleSummaryResponse(
    @Schema(description = "Entered Sample IDs: 정규화·중복 제거 후 입력한 Sample ID 수", example = "30")
    int enteredSampleIdCount,
    @Schema(description = "Found Sample IDs: 현재 검색 조건에 맞는 리포트가 있는 고유 Sample ID 수", example = "30")
    long foundSampleIdCount,
    @Schema(description = "Report: 현재 검색 조건의 전체 리포트 수", example = "90")
    long reportCount,
    @Schema(description = "No Results: ID는 존재하지만 현재 검색 조건에 맞는 리포트가 없는 Sample ID 수", example = "0")
    long noResultsCount,
    @Schema(description = "Not Found: 검체 목록에 존재하지 않는 Sample ID 수", example = "0")
    long notFoundCount
) {
    public static PatientSampleSummaryResponse empty() {
        return new PatientSampleSummaryResponse(0, 0, 0, 0, 0);
    }
}
