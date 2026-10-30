package com.stagora.report.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.stagora.report.dto.vo.InstitutionInfoVO;

@Mapper
public interface InstitutionMapper {
	InstitutionInfoVO getHospitalInfo(String hospitalId);

	InstitutionInfoVO getClabInfo(String clabId);
}
