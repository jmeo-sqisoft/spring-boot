package com.seegene.local.server.service;

import com.seegene.local.server.dto.vo.AreaVO;
import com.seegene.local.server.mapper.AreaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AreaService {
	private final AreaMapper areaMapper;

	public List<AreaVO> getArea() {
		return areaMapper.getArea();
	}
}
