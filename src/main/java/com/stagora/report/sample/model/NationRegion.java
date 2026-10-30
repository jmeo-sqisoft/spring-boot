package com.stagora.report.sample.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "국가 하위 지역 정보")
public record NationRegion(
    @Schema(description = "국가 하위 지역 아이디", example = "11") Integer nationRegionNo,
    @Schema(description = "국가 하위 지역 코드", example = "11") String nationRegionCode,
    @Schema(description = "국가 하위 지역명", example = "Seoul") String nationRegionName
) {
}
