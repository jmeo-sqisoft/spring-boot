package com.stagora.report.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.stagora.report.dto.vo.AreaVO;

@Mapper
public interface AreaMapper {

	List<AreaVO> getArea();
}
