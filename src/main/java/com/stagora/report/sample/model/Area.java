package com.stagora.report.sample.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 공통 코드에서 조회한 지역 코드와 이름이다.
 */
@Schema(description = "지역 정보 조회 모델")
public record Area(
    @Schema(description = "지역 코드", example = "AS") String areaCode,
    @Schema(description = "지역명", example = "ASIA") String areaName
) {
}
