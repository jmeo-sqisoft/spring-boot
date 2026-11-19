package com.stagora.reports.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "환자 목록의 전체 검색 결과 집계 모델")
public record PatientListStatistics(
    @Schema(description = "전체 일치 리포트 수") long reportCount,
    @Schema(description = "일치 리포트가 있는 고유 Sample ID 수") long foundSampleIdCount,
    @Schema(description = "존재하지만 일치 리포트가 없는 Sample ID 수") long noResultsCount,
    @Schema(description = "존재하지 않는 Sample ID 수") long notFoundCount,
    @Schema(description = "일치 결과의 RTI 리포트 수") long rtiReportCount,
    @Schema(description = "일치 결과의 ABI 리포트 수") long abiReportCount,
    @Schema(description = "일치 결과의 GTI 리포트 수") long gtiReportCount
) {
}
