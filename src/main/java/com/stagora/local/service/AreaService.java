package com.stagora.local.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.stagora.local.dto.mapper.AreasResponseMapper;
import com.stagora.local.dto.response.AreasResponse;
import com.stagora.local.dto.vo.AreaVO;
import com.stagora.local.mapper.AreaMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AreaService {
	private final AreaMapper areaMapper;
	private final AreasResponseMapper areasResponseMapper;

	public List<AreasResponse> getArea() {
		List<AreaVO> areaInfo = areaMapper.getArea();
		return areasResponseMapper.toResponses(areaInfo);
	}
}
