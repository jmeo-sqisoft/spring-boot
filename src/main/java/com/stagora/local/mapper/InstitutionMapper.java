package com.stagora.local.mapper;

import com.stagora.local.dto.vo.InstitutionInfoVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InstitutionMapper {
	InstitutionInfoVO getHospitalInfo(String hospitalId);

	InstitutionInfoVO getClabInfo(String clabId);
}
