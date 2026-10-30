package com.stagora.report.sample.model;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "국가 정보 응답 DTO")
public class NationInfo {
    @Schema(description = "국가 정보 번호", example = "12332")
    private Long nationInfoNo;
    @Schema(description = "국가 코드", example = "KR")
    private String nationCode;
    @Schema(description = "국가명", example = "Republic of Korea")
    private String nationName;
    @Schema(description = "위도", example = "36.549855")
    private BigDecimal latitude;
    @Schema(description = "경도", example = "127.83961")
    private BigDecimal longitude;
    @Schema(description = "참여 여부", example = "true")
    private Boolean isParticipating;
    @Schema(description = "국가 하위 지역 목록")
    private List<NationRegion> nationRegions;
}
