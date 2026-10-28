package com.stagora.local.controller;

import com.stagora.common.controller.AbstractApiController;
import com.stagora.common.dto.response.SspResponse;
import com.stagora.common.enums.ApiResponseCode;
import com.stagora.common.exception.ApiException;
import com.stagora.local.dto.response.InstitutionInfoResponse;
import com.stagora.local.service.InstitutionService;
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

@Tag(name = "Institution", description = "기관정보 api")
@RestController
@RequestMapping("/apis/gmcs/v1")
@RequiredArgsConstructor
public class InstitutionController extends AbstractApiController {
	private final MessageSource messageSource;
	private final InstitutionService institutionService;

	@Operation(summary = "기관정보 조회", description = "clabId 또는 hospitalId 중 하나 이상 필요하며, hospitalId가 있으면 병원을 조회합니다.")
	@GetMapping("/institutioninfo")
	public ResponseEntity<SspResponse<InstitutionInfoResponse>> institutionInfo(
		@Schema(description = "검사실 ID", example = "CL0001") @RequestParam(required = false) String clabId,
		@Schema(description = "병원 ID", example = "019bf94a-1e58-773c-9d46-ae8318e6bcba") @RequestParam(required = false) String hospitalId) {
		if (StringUtils.trimToNull(clabId) == null && StringUtils.trimToNull(hospitalId) == null) {
			throw new ApiException(ApiResponseCode.E00400);
		}
		InstitutionInfoResponse response = institutionService.getInstitutionInfo(clabId, hospitalId);
		return success(response);
	}

	@Override
	protected MessageSource getMessageSource() {
		return messageSource;
	}
}
