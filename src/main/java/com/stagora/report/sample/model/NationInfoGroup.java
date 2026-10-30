package com.stagora.report.sample.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * MyBatis 중첩 결과 매핑에서 지역별 국가 목록을 묶는 조회 모델이다.
 */
@Getter
@Setter
public class NationInfoGroup {
    private String areaCode;
    private String areaName;
    // 국가별 조회 결과와 각 국가의 하위 지역 목록
    private List<NationInfo> nationInfoVOs;
}
