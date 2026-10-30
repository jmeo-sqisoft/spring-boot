package com.stagora.report.sample.dto;

import java.sql.Timestamp;

import com.stagora.global.enums.InstitutionType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 기관의 식별 정보, 표시 유형, 계약 정보와 연락처를 담는 조회 응답이다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "기관 정보 조회 응답")
public class InstitutionInfoResponse {
    @Schema(description = "기관 ID", example = "CL0001")
    private String institutionId;

    @Schema(description = "기관명", example = "씨젠의료재단")
    private String institutionName;

    @Schema(description = "기관 표시 유형. 제휴병원도 검사 집계는 CLAB 기준", example = "AFFILIATED")
    private InstitutionType institutionType;

    @Schema(description = "도시", example = "Seoul")
    private String institutionCity;

    @Schema(description = "국가 코드", example = "KR")
    private String institutionNationCode;

    @Schema(description = "국가명", example = "Republic of Korea")
    private String institutionNation;

    @Schema(description = "대륙 코드", example = "AS")
    private String institutionArea;

    @Schema(description = "가입 일시", example = "2026-08-03T06:54:03.508Z")
    private Timestamp registrationDate;

    @Schema(description = "계약 만료 일시", example = "2026-12-03T00:00:00Z")
    private Timestamp expirationDate;

    @Schema(description = "이메일 주소", example = "abc@seegenemedical.com")
    private String contactEmail;

    @Schema(description = "데이터 유형", example = "LIS")
    private String dataType;
}
