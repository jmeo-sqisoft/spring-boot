package com.seegene.local.server.dto.vo;

import com.seegene.local.base.enums.InstitutionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.sql.Timestamp;

@Schema(description = "기관정보 VO")
public record InstitutionInfoVO(
	@Schema(description = "기관 아이디", example = "01a00661-5b8c-70b7-801e-155c610ecd5f") String InstitutionId,
	@Schema(description = "기관명", example = "씨젠의료재단") String InstitutionName,
	@Schema(description = "기관 표시 유형. 제휴병원도 검사 집계는 CLAB 기준", example = "AFFILIATED") InstitutionType InstitutionType,
	@Schema(description = "도시", example = "Seoul") String InstitutionCity,
	@Schema(description = "국가 코드", example = "KR") String InstitutionNationCode,
	@Schema(description = "국가명", example = "Republic of korea") String InstitutionNation,
	@Schema(description = "대륙코드", example = "AS") String InstitutionArea,
	@Schema(description = "가입일시", example = "2026-08-03 15:54:03.508 +0900") Timestamp registrationDate,
	@Schema(description = "계약만료일시", example = "2026-12-03 00:54:03.508 +0900") Timestamp expirationDate,
	@Schema(description = "이메일주소", example = "abc@seegenemedical.com") String contactEmail,
	@Schema(description = "데이터구분", example = "LIS") String dataType
) {
}
