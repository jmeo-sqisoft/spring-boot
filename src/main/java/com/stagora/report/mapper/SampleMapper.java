package com.stagora.report.mapper;

import com.stagora.report.dto.vo.AreaVO;
import com.stagora.report.dto.vo.InstitutionInfoVO;
import com.stagora.report.dto.vo.NationInfoGroupVO;
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
