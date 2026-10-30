package com.stagora.report.sample.model;

import java.sql.Timestamp;

import com.stagora.global.enums.InstitutionType;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 병원 또는 검사실 조회 SQL의 결과를 담는 기관 정보 모델이다.
 */
@Schema(description = "기관 정보 조회 모델")
public record InstitutionInfo(
    @Schema(description = "기관 ID", example = "01a00661-5b8c-70b7-801e-155c610ecd5f")
    String institutionId,
    @Schema(description = "기관명", example = "씨젠의료재단")
    String institutionName,
    @Schema(description = "기관 표시 유형. 제휴병원도 검사 집계는 CLAB 기준", example = "AFFILIATED")
    InstitutionType institutionType,
    @Schema(description = "도시", example = "Seoul")
    String institutionCity,
    @Schema(description = "국가 코드", example = "KR")
    String institutionNationCode,
    @Schema(description = "국가명", example = "Republic of korea")
    String institutionNation,
    @Schema(description = "대륙 코드", example = "AS")
    String institutionArea,
    @Schema(description = "가입 일시", example = "2026-08-03 15:54:03.508 +0900")
    Timestamp registrationDate,
    @Schema(description = "계약 만료 일시", example = "2026-12-03 00:54:03.508 +0900")
    Timestamp expirationDate,
    @Schema(description = "이메일 주소", example = "abc@seegenemedical.com")
    String contactEmail,
    @Schema(description = "데이터 유형", example = "LIS")
    String dataType
) {
}
