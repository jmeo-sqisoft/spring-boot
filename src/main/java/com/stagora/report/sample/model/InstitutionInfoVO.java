package com.stagora.report.sample.model;

import java.sql.Timestamp;

import com.stagora.global.enums.InstitutionType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "기관정보 VO")
public record InstitutionInfoVO(
	@Schema(description = "기관 아이디", example = "01a00661-5b8c-70b7-801e-155c610ecd5f") String institutionId,
	@Schema(description = "기관명", example = "씨젠의료재단") String institutionName,
	@Schema(description = "기관 표시 유형. 제휴병원도 검사 집계는 CLAB 기준", example = "AFFILIATED") InstitutionType institutionType,
	@Schema(description = "도시", example = "Seoul") String institutionCity,
	@Schema(description = "국가 코드", example = "KR") String institutionNationCode,
	@Schema(description = "국가명", example = "Republic of korea") String institutionNation,
	@Schema(description = "대륙코드", example = "AS") String institutionArea,
	@Schema(description = "가입일시", example = "2026-08-03 15:54:03.508 +0900") Timestamp registrationDate,
	@Schema(description = "계약만료일시", example = "2026-12-03 00:54:03.508 +0900") Timestamp expirationDate,
	@Schema(description = "이메일주소", example = "abc@seegenemedical.com") String contactEmail,
	@Schema(description = "데이터구분", example = "LIS") String dataType
) {
}
