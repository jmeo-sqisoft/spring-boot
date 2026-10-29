package com.stagora.local.mapper;

import com.stagora.local.dto.vo.AreaVO;
import com.stagora.local.dto.vo.InstitutionInfoVO;
import com.stagora.local.dto.vo.NationInfoGroupVO;
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
