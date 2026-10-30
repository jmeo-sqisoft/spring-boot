package com.stagora.report.sample.dto;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import com.stagora.report.sample.model.Area;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AreasConverter {

    AreasResponse toResponse(Area area);

    List<AreasResponse> toResponses(List<Area> areas);
}
