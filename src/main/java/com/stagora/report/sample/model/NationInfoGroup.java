package com.stagora.report.sample.model;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * MyBatis 중첩 결과 매핑에서 지역별 국가 목록을 묶는 조회 모델이다.
 */
@Getter
@Setter
@Schema(description = "지역별 국가와 국가 하위 지역을 묶는 DB 조회 모델")
public class NationInfoGroup {
    @Schema(description = "지역 코드", example = "AS")
    private String areaCode;
    @Schema(description = "지역명", example = "Asia")
    private String areaName;
    // 국가별 조회 결과와 각 국가의 하위 지역 목록
    @Schema(description = "국가별 조회 결과와 각 국가의 하위 지역 목록")
    private List<NationInfo> nationInfoVOs;
}
