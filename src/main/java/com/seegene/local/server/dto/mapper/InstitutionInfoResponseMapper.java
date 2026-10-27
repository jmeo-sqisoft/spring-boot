package com.seegene.local.server.dto.mapper;

import com.seegene.local.server.dto.response.InstitutionInfoResponse;
import com.seegene.local.server.dto.vo.InstitutionInfoVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface InstitutionInfoResponseMapper {

	InstitutionInfoResponse toResponse(InstitutionInfoVO info);
}
