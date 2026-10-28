package com.stagora.local.mapper;

import com.stagora.local.dto.vo.AreaVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AreaMapper {

	List<AreaVO> getArea();
}
