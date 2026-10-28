package com.stagora.local.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.stagora.local.dto.vo.InstitutionInfoVO;

@Mapper
public interface InstitutionMapper {
	InstitutionInfoVO getHospitalInfo(String hospitalId);

	InstitutionInfoVO getClabInfo(String clabId);
}
