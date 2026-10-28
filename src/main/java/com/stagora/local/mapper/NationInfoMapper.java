package com.stagora.local.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.stagora.local.dto.vo.NationInfoGroupVO;

@Mapper
public interface NationInfoMapper {

	List<NationInfoGroupVO> getNationInfo(@Param("lang") String lang);
}
