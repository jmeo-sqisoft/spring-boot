package com.stagora.local.dto.mapper;

import com.stagora.local.dto.response.NationInfoResponse;
import com.stagora.local.dto.vo.NationInfoGroupVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface NationInfoResponseMapper {

	NationInfoResponse toResponse(NationInfoGroupVO group);

	List<NationInfoResponse> toResponses(List<NationInfoGroupVO> groups);
}
