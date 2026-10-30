package com.stagora.report.sample.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.stagora.report.sample.model.Area;
import com.stagora.report.sample.model.InstitutionInfo;
import com.stagora.report.sample.model.NationInfoGroup;

@Mapper
public interface SampleMapper {
    List<Area> getArea();

    InstitutionInfo getHospitalInfo(String hospitalId);

    InstitutionInfo getClabInfo(String clabId);

    List<NationInfoGroup> getNationInfo(@Param("lang") String lang);
}
