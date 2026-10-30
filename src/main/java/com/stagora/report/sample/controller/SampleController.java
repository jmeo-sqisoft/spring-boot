package com.stagora.report.sample.controller;

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stagora.global.api.AbstractApiController;
import com.stagora.global.api.ApiResponseCode;
import com.stagora.global.api.StaResponse;
import com.stagora.global.exception.ApiException;
import com.stagora.report.sample.dto.AreasResponse;
import com.stagora.report.sample.dto.InstitutionInfoRequest;
import com.stagora.report.sample.dto.InstitutionInfoResponse;
import com.stagora.report.sample.dto.NationInfoResponse;
import com.stagora.report.sample.service.SampleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 샘플 API를 제공한다.
 */
@Tag(name = "Sample", description = "Sample API")
@RestController
@RequestMapping("/apis/report/v1")
@RequiredArgsConstructor
public class SampleController extends AbstractApiController {
    private final MessageSource messageSource;
    private final SampleService sampleService;

    /**
     * 지역 목록을 조회해 공통 성공 응답으로 반환한다.
     */
    @Operation(summary = "지역 목록 조회", description = "사용 중인 지역 코드와 이름을 조회합니다.")
    @GetMapping("/sample/areas")
    public ResponseEntity<StaResponse<List<AreasResponse>>> getAreas() {
        List<AreasResponse> response = sampleService.getArea();
        return super.success(response);
    }

    /**
     * 기관 식별자를 검증하고 요청 객체로 묶어 서비스에 전달한다.
     * 두 식별자가 모두 없거나 공백뿐이면 E00400 예외를 발생시킨다.
     */
    @Operation(summary = "기관 정보 조회",
        description = "clabId 또는 hospitalId 중 하나 이상 필요하며, hospitalId가 있으면 병원을 조회합니다.")
    @GetMapping("/sample/institutioninfo")
    public ResponseEntity<StaResponse<InstitutionInfoResponse>> institutionInfo(
        @Schema(description = "검사실 ID", example = "CL0001")
        @RequestParam(required = false) String clabId,
        @Schema(description = "병원 ID", example = "019bf94a-1e58-773c-9d46-ae8318e6bcba")
        @RequestParam(required = false) String hospitalId) {
        if (StringUtils.trimToNull(clabId) == null && StringUtils.trimToNull(hospitalId) == null) {
            throw new ApiException(ApiResponseCode.E00400);
        }
        InstitutionInfoRequest request = new InstitutionInfoRequest();
        request.setClabId(clabId);
        request.setHospitalId(hospitalId);
        InstitutionInfoResponse response = sampleService.getInstitutionInfo(request);
        return success(response);
    }

    /**
     * 요청 로케일에 따른 지역별 국가 목록을 공통 성공 응답으로 반환한다.
     */
    @Operation(summary = "지역별 국가 목록 조회",
        description = "요청 언어에 따라 지역·국가·하위 지역을 조회하고 지역명과 국가명으로 정렬합니다.")
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
