package com.stagora.report.sample.dto;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import com.stagora.report.sample.model.Area;

/**
 * 지역 조회 모델을 API 응답 객체로 변환한다.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AreasConverter {
    /**
     * 지역 코드와 이름을 응답에 복사한다. 입력이 null이면 null을 반환한다.
     */
    AreasResponse toResponse(Area area);

    /**
     * 입력 순서를 유지해 목록을 변환한다. null 목록은 null, 빈 목록은 빈 목록을 반환한다.
     */
    List<AreasResponse> toResponses(List<Area> areas);
}
