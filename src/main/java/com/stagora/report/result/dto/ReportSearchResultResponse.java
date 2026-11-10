package com.stagora.report.result.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리포트 조회 건수와 입력 순서의 결과 목록")
public record ReportSearchResultResponse(
    @Schema(description = "정규화·중복 제거를 마친 입력 ID 수", example = "4", minimum = "1")
    int requestedCount,
    @Schema(description = "입력 ID 중 조건에 맞는 리포트가 조회된 수", example = "3", minimum = "0")
    int foundCount,
    @Schema(description = "입력 ID 중 현재 조회 조건에서 미조회된 수", example = "1", minimum = "0")
    int missingCount,
    @Schema(description = "입력 ID 순서의 결과 목록. 미조회 항목을 포함하며 크기는 requestedCount와 같음")
    List<ReportItemResponse> items
) {
}
