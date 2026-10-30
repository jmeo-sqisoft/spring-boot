package com.stagora.report.dto.converter;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import com.stagora.report.sample.dto.AreasResponse;
import com.stagora.report.sample.model.AreaVO;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AreasConverter {

	AreasResponse toResponse(AreaVO area);

	List<AreasResponse> toResponses(List<AreaVO> areas);
}
