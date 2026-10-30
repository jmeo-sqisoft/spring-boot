package com.stagora.report.sample.dto;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import com.stagora.report.sample.model.InstitutionInfo;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface InstitutionInfoConverter {
    InstitutionInfoResponse toResponse(InstitutionInfo info);
}
