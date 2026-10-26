package com.seegene.local.server.mapper;

import com.seegene.local.server.dto.vo.AreaVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AreaMapper {

	List<AreaVO> getArea();
}
