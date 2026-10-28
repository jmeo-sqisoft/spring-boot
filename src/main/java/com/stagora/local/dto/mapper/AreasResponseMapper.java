package com.stagora.local.dto.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import com.stagora.local.dto.response.AreasResponse;
import com.stagora.local.dto.vo.AreaVO;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AreasResponseMapper {

	AreasResponse toResponse(AreaVO area);

	List<AreasResponse> toResponses(List<AreaVO> areas);
}
