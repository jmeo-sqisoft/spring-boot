package com.stagora.report.sample.dto;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import com.stagora.report.sample.model.NationInfoGroup;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface NationInfoConverter {

	NationInfoResponse toResponse(NationInfoGroup group);

	List<NationInfoResponse> toResponses(List<NationInfoGroup> groups);
}
