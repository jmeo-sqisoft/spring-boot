package com.stagora.local.dto.converter;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import com.stagora.local.dto.response.NationInfoResponse;
import com.stagora.local.dto.vo.NationInfoGroupVO;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface NationInfoConverter {

	NationInfoResponse toResponse(NationInfoGroupVO group);

	List<NationInfoResponse> toResponses(List<NationInfoGroupVO> groups);
}
