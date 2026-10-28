package com.stagora.local.dto.mapper;

import com.stagora.local.dto.response.AreasResponse;
import com.stagora.local.dto.vo.AreaVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AreasResponseMapper {

	AreasResponse toResponse(AreaVO area);

	List<AreasResponse> toResponses(List<AreaVO> areas);
}
