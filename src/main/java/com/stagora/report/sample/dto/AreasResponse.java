package com.stagora.report.sample.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "기관정보 조회 DTO")
public class AreasResponse {
    @Schema(description = "지역 코드", example = "AS")
    private String areaCode;
    @Schema(description = "지역명", example = "ASIA")
    private String areaName;
}
