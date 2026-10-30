package com.stagora.report.controller;

import java.util.List;

import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stagora.global.api.AbstractApiController;
import com.stagora.global.api.StaResponse;
import com.stagora.report.dto.response.AreasResponse;
import com.stagora.report.service.AreaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Area", description = "지역 정보 API")
@RestController
@RequestMapping("/apis/local/v1")
@RequiredArgsConstructor
public class AreaController extends AbstractApiController {
	private final MessageSource messageSource;
	private final AreaService areaService;

	@Operation(summary = "지역 정보 전체 목록", description = "지역 정보 전체를 전달한다.")
	@GetMapping("/areas")
	public ResponseEntity<StaResponse<List<AreasResponse>>> getAreas() {
		List<AreasResponse> response = areaService.getArea();
		return super.success(response);
	}

	@Override
	protected MessageSource getMessageSource() {
		return messageSource;
	}
}
