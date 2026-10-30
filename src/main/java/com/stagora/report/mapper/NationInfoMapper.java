package com.stagora.report.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.stagora.report.dto.vo.NationInfoGroupVO;

@Mapper
public interface NationInfoMapper {

	List<NationInfoGroupVO> getNationInfo(@Param("lang") String lang);
}
