package com.stagora.report.sample.mapper;

import com.stagora.report.sample.model.Area;
import com.stagora.report.sample.model.InstitutionInfo;
import com.stagora.report.sample.model.NationInfoGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SampleMapper {
	List<Area> getArea();

	InstitutionInfo getHospitalInfo(String hospitalId);

	InstitutionInfo getClabInfo(String clabId);

	List<NationInfoGroup> getNationInfo(@Param("lang") String lang);
}
