package com.stagora.local.controller;

import com.stagora.common.api.AbstractApiController;
import com.stagora.common.api.ApiResponseCode;
import com.stagora.common.api.StaResponse;
import com.stagora.common.exception.ApiException;
import com.stagora.local.dto.response.AreasResponse;
import com.stagora.local.dto.response.InstitutionInfoResponse;
import com.stagora.local.dto.response.NationInfoResponse;
import com.stagora.local.service.SampleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@Tag(name = "Sample", description = "Sample API")
@RestController
@RequestMapping("/apis/local/v1")
@RequiredArgsConstructor
public class SampleController extends AbstractApiController {
	private final MessageSource messageSource;
	private final SampleService sampleService;

	@Operation(summary = "지역 정보 전체 목록", description = "지역 정보 전체를 전달한다.")
	@GetMapping("/sample/areas")
	public ResponseEntity<StaResponse<List<AreasResponse>>> getAreas() {
		List<AreasResponse> response = sampleService.getArea();
		return super.success(response);
	}

	@Operation(summary = "기관정보 조회", description = "clabId 또는 hospitalId 중 하나 이상 필요하며, hospitalId가 있으면 병원을 조회합니다.")
	@GetMapping("/sample/institutioninfo")
	public ResponseEntity<StaResponse<InstitutionInfoResponse>> institutionInfo(
		@Schema(description = "검사실 ID", example = "CL0001") @RequestParam(required = false) String clabId,
		@Schema(description = "병원 ID", example = "019bf94a-1e58-773c-9d46-ae8318e6bcba") @RequestParam(required = false) String hospitalId) {
		if (StringUtils.trimToNull(clabId) == null && StringUtils.trimToNull(hospitalId) == null) {
			throw new ApiException(ApiResponseCode.E00400);
		}
		InstitutionInfoResponse response = sampleService.getInstitutionInfo(clabId, hospitalId);
		return success(response);
	}

	@Operation(summary = "국가 정보 전체 목록", description = "국가 정보 전체를 전달한다. areaCd로 지역별 필터링이 가능하다.")
	@GetMapping("/sample/nations")
	public ResponseEntity<StaResponse<List<NationInfoResponse>>> getNations(Locale locale) {
		List<NationInfoResponse> response = sampleService.getNationInfo(locale);
		return super.success(response);
	}

	@Override
	protected MessageSource getMessageSource() {
		return messageSource;
	}
}
