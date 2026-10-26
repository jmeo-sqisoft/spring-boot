package com.seegene.local.server.controller;

import com.seegene.local.base.controller.AbstractApiController;
import com.seegene.local.base.dto.response.SspResponse;
import com.seegene.local.server.dto.vo.AreaVO;
import com.seegene.local.server.service.AreaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Area", description = "지역 정보 API")
@RestController
@RequestMapping("/apis/local/v1")
@RequiredArgsConstructor
public class AreaController extends AbstractApiController {

	private final MessageSource messageSource;
	private final AreaService areaService;

	@Operation(summary = "지역 정보 전체 목록", description = "지역 정보 전체를 전달한다.")
	@GetMapping("/masterdata/areas")
	public ResponseEntity<SspResponse<List<AreaVO>>> getAreas() {
		return super.success(areaService.getArea());
	}

	@Override
	protected MessageSource getMessageSource() {
		return messageSource;
	}
}
