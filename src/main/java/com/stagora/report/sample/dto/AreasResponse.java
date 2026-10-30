package com.stagora.report.sample.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 지역 목록 조회에서 반환하는 지역 코드와 이름이다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "지역 정보 조회 응답")
public class AreasResponse {
    @Schema(description = "지역 코드", example = "AS")
    private String areaCode;
    @Schema(description = "지역명", example = "ASIA")
    private String areaName;
}
