package com.stagora.report.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.stagora.report.dto.converter.AreasConverter;
import com.stagora.report.dto.response.AreasResponse;
import com.stagora.report.dto.vo.AreaVO;
import com.stagora.report.mapper.AreaMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AreaService {
	private final AreaMapper areaMapper;
	private final AreasConverter areasConverter;

	public List<AreasResponse> getArea() {
		List<AreaVO> areaInfo = areaMapper.getArea();
		return areasConverter.toResponses(areaInfo);
	}
}
