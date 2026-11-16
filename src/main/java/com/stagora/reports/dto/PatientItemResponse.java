package com.stagora.reports.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "환자 리포트 목록의 한 행. 같은 Sample ID에 여러 검사 리포트가 있을 수 있음")
public record PatientItemResponse(
    @Schema(description = "행을 구분하는 리포트 UUID")
    String reportId,
    @Schema(description = "검체 일련번호", example = "S26-1015-0231")
    String sampleId,
    @Schema(description = "검사 분류 코드", example = "COMPREHENSIVE_PANEL")
    String testClassification,
    @Schema(description = "질환 코드", example = "HPV_STI")
    String disease,
    @Schema(description = "질환 전체 이름. 툴팁 표시용")
    String diseaseName,
    @Schema(description = "검사 패널 코드", example = "RTI")
    String panelCode,
    @Schema(description = "검사 패널 전체 이름. 툴팁 표시용")
    String panelName,
    @Schema(description = "검사일")
    LocalDate testDate,
    @Schema(description = "검출된 분석 항목 수. 양성 건수와 별개", example = "10", minimum = "0")
    int detectedAnalytesCount,
    @Schema(description = "리포트 생성일")
    LocalDate reportCreatedDate,
    @Schema(description = "출력 횟수. 0이면 Not Printed", minimum = "0")
    int printCount,
    @Schema(description = "마지막 출력 일시. 출력 전이면 null")
    LocalDateTime lastPrintedAt
) {
}
