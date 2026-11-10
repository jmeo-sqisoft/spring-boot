package com.stagora.report.result.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "개별 리포트의 검사 정보와 기존 출력 이력")
public record ReportSearchResponse(
    @Schema(description = "검체 일련번호", example = "S26-1015-0232")
    String sampleId,
    @Schema(description = "검사 패널 코드", example = "ABI")
    String panelCode,
    @Schema(description = "검사 패널 이름", example = "호흡기 감염 종합")
    String panelName,
    @Schema(description = "검사일", example = "2026-10-15")
    LocalDate testDate,
    @Schema(description = "양성 결과 건수", example = "5", minimum = "0")
    int positiveCount,
    @Schema(description = "리포트 생성일", example = "2026-10-15")
    LocalDate reportCreatedDate,
    @Schema(description = "기존 출력 횟수. 출력 전이면 0", example = "1", minimum = "0")
    int printCount,
    @Schema(description = "마지막 출력 일시. 출력 전이면 null", example = "2026-10-16T00:00:00")
    LocalDateTime lastPrintedAt
) {
}
