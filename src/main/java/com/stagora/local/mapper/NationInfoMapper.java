package com.stagora.local.mapper;

import com.stagora.local.dto.vo.NationInfoGroupVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NationInfoMapper {

	List<NationInfoGroupVO> getNationInfo(@Param("lang") String lang);
}
