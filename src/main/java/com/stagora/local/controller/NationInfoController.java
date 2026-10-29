package com.stagora.local.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stagora.common.api.AbstractApiController;
import com.stagora.common.api.StaResponse;
import com.stagora.local.dto.response.NationInfoResponse;
import com.stagora.local.service.NationInfoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "NationInfo", description = "국가 정보 API")
@RestController
@RequestMapping("/apis/gmcs/v1")
@RequiredArgsConstructor
public class NationInfoController extends AbstractApiController {
	private final MessageSource messageSource;
	private final NationInfoService nationInfoService;

	@Operation(summary = "국가 정보 전체 목록", description = "국가 정보 전체를 전달한다. areaCd로 지역별 필터링이 가능하다.")
	@GetMapping("/nations")
	public ResponseEntity<StaResponse<List<NationInfoResponse>>> getNations(Locale locale) {
		List<NationInfoResponse> response = nationInfoService.getNationInfo(locale);
		return super.success(response);
	}

	@Override
	protected MessageSource getMessageSource() {
		return messageSource;
	}
}
