package com.stagora.local.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.stagora.local.dto.vo.AreaVO;

@Mapper
public interface AreaMapper {

	List<AreaVO> getArea();
}
