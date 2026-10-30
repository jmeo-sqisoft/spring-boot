package com.stagora.report.sample.dto;

import java.util.List;

import com.stagora.report.sample.model.NationInfo;

import lombok.Getter;
import lombok.Setter;

/**
 * 지역 코드와 이름, 해당 지역의 국가·하위 지역 목록을 담는 조회 응답이다.
 */
@Getter
@Setter
public class NationInfoResponse {
    // 국가 목록을 묶는 지역 정보
    private String areaCode;
    private String areaName;

    // 국가별 좌표, 참여 여부와 하위 지역 정보를 포함한다.
    private List<NationInfo> nationInfoVOs;
}
