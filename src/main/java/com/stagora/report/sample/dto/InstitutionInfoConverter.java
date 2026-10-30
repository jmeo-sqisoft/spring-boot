package com.stagora.report.sample.dto;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import com.stagora.report.sample.model.InstitutionInfo;

/**
 * 기관 조회 모델을 API 응답 객체로 변환한다.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface InstitutionInfoConverter {
    /**
     * 이름이 같은 기관 정보 필드를 응답에 복사한다. 입력이 null이면 null을 반환한다.
     */
    InstitutionInfoResponse toResponse(InstitutionInfo info);
}
