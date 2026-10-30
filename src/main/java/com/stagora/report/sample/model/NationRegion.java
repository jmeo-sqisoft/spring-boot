package com.stagora.report.sample.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 국가에 속한 하위 지역의 번호, 코드와 이름을 담는 조회 모델이다.
 */
@Schema(description = "국가 하위 지역 조회 모델")
public record NationRegion(
    @Schema(description = "국가 하위 지역 번호", example = "11") Integer nationRegionNo,
    @Schema(description = "국가 하위 지역 코드", example = "11") String nationRegionCode,
    @Schema(description = "국가 하위 지역명", example = "Seoul") String nationRegionName
) {
}
