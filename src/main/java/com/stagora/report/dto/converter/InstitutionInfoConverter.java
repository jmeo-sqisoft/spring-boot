package com.stagora.report.dto.converter;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import com.stagora.report.sample.dto.InstitutionInfoResponse;
import com.stagora.report.sample.model.InstitutionInfoVO;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface InstitutionInfoConverter {

	InstitutionInfoResponse toResponse(InstitutionInfoVO info);
}
