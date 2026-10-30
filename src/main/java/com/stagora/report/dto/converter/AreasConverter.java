package com.stagora.report.dto.converter;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import com.stagora.report.dto.response.AreasResponse;
import com.stagora.report.dto.vo.AreaVO;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AreasConverter {

	AreasResponse toResponse(AreaVO area);

	List<AreasResponse> toResponses(List<AreaVO> areas);
}
