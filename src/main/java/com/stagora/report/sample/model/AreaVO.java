package com.stagora.report.sample.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지역 정보 응답 DTO")
public record AreaVO(
	@Schema(description = "지역 코드", example = "AS") String areaCode,
	@Schema(description = "지역명", example = "ASIA") String areaName
) {
}
