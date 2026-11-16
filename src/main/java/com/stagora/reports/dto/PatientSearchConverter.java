package com.stagora.reports.dto;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import com.stagora.reports.model.PatientListItem;
import com.stagora.reports.model.PatientSearch;

/**
 * 리포트 조회 모델을 API 응답 객체로 변환한다.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PatientSearchConverter {
    /**
     * 이름이 같은 리포트 정보 필드를 응답에 복사한다. 입력이 null이면 null을 반환한다.
     */
    PatientSearchResponse toResponse(PatientSearch report);

    /**
     * 목록 전용 조회 모델의 분류·질환·출력 정보를 목록 응답으로 변환한다.
     */
    PatientItemResponse toItemResponse(PatientListItem report);
}
