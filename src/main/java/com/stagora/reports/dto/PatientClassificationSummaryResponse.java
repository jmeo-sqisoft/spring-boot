package com.stagora.reports.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "검사 분류 검색 요약. Sample ID 검색에서는 모든 건수가 0")
public record PatientClassificationSummaryResponse(
    @Schema(description = "페이지 제한 전 전체 일치 리포트 수", example = "5")
    long reportCount,
    @Schema(description = "조회된 리포트의 고유 Sample ID 수", example = "4")
    long foundSampleIdCount,
    @Schema(description = "RTI 리포트 수. 조회되지 않으면 0", example = "2")
    long panelRtiCount,
    @Schema(description = "ABI 리포트 수. 조회되지 않으면 0", example = "2")
    long panelAbiCount,
    @Schema(description = "GTI 리포트 수. 조회되지 않으면 0", example = "1")
    long panelGtiCount
) {
    public static PatientClassificationSummaryResponse empty() {
        return new PatientClassificationSummaryResponse(0, 0, 0, 0, 0);
    }
}
