package com.seegene.local.server.mapper;

import com.seegene.local.server.dto.vo.InstitutionInfoVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InstitutionMapper {
	InstitutionInfoVO getHospitalInfo(String hospitalId);

	InstitutionInfoVO getClabInfo(String clabId);
}
