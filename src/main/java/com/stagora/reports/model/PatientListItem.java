package com.stagora.reports.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 분류·질환·검출 항목 수를 포함하는 목록 전용 조회 모델이다.
 */
@Schema(description = "환자 리포트 목록 조회 모델")
public record PatientListItem(
    @Schema(description = "리포트 UUID") String reportId,
    @Schema(description = "검체 일련번호") String sampleId,
    @Schema(description = "검사 분류 코드") String testClassification,
    @Schema(description = "질환 코드") String disease,
    @Schema(description = "질환 전체 이름") String diseaseName,
    @Schema(description = "검사 패널 코드") String panelCode,
    @Schema(description = "검사 패널 전체 이름") String panelName,
    @Schema(description = "검사일") LocalDate testDate,
    @Schema(description = "검출된 분석 항목 수") int detectedAnalytesCount,
    @Schema(description = "리포트 생성일") LocalDate reportCreatedDate,
    @Schema(description = "출력 횟수") int printCount,
    @Schema(description = "마지막 출력 일시") LocalDateTime lastPrintedAt
) {
}
