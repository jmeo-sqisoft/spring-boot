package com.stagora.report.sample.mapper;

import com.stagora.report.sample.model.AreaVO;
import com.stagora.report.sample.model.InstitutionInfoVO;
import com.stagora.report.sample.model.NationInfoGroupVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SampleMapper {
	List<AreaVO> getArea();

	InstitutionInfoVO getHospitalInfo(String hospitalId);

	InstitutionInfoVO getClabInfo(String clabId);

	List<NationInfoGroupVO> getNationInfo(@Param("lang") String lang);
}
