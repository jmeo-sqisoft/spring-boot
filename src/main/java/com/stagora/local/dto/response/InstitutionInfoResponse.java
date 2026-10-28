package com.stagora.local.dto.response;

import com.stagora.common.enums.InstitutionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "기관정보 조회 DTO")
public class InstitutionInfoResponse {
	@Schema(description = "기관 아이디", example = "CL0001")
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

	@Schema(description = "대륙코드", example = "AS")
	private String institutionArea;

	@Schema(description = "가입일시", example = "2026-08-03T06:54:03.508Z")
	private Timestamp registrationDate;

	@Schema(description = "계약만료일시", example = "2026-12-03T00:00:00Z")
	private Timestamp expirationDate;

	@Schema(description = "이메일주소", example = "abc@seegenemedical.com")
	private String contactEmail;

	@Schema(description = "데이터구분", example = "LIS")
	private String dataType;
}
