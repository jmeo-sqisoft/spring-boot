package com.stagora.local.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import com.stagora.local.dto.response.InstitutionInfoResponse;
import com.stagora.local.dto.vo.InstitutionInfoVO;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface InstitutionInfoResponseMapper {

	InstitutionInfoResponse toResponse(InstitutionInfoVO info);
}
