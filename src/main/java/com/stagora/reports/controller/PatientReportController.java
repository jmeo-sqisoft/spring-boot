package com.stagora.reports.controller;

import org.springframework.context.MessageSource;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stagora.global.api.AbstractApiController;
import com.stagora.global.api.ApiResponseCode;
import com.stagora.global.api.StaResponse;
import com.stagora.global.exception.ApiException;
import com.stagora.reports.dto.PatientDetailResponse;
import com.stagora.reports.dto.PatientDetailSearchRequest;
import com.stagora.reports.dto.PatientListResponse;
import com.stagora.reports.dto.PatientListSearchRequest;
import com.stagora.reports.service.PatientReportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Patient Report", description = "환자 리포트 API")
@RestController
@RequestMapping("/apis/report/v1/patient")
@RequiredArgsConstructor
public class PatientReportController extends AbstractApiController {
    private final MessageSource messageSource;
    private final PatientReportService patientReportService;

    @Operation(summary = "환자 리포트 목록 조회",
        description = "Sample ID는 필수이며, 입력 순서대로 리포트와 미조회 항목을 반환합니다.")
    @PostMapping("/list")
    public ResponseEntity<StaResponse<PatientListResponse>> getPatientList(
        @RequestBody(required = false) PatientListSearchRequest request) {
        if (request == null) {
            throw new ApiException(ApiResponseCode.E40001);
        }
        return success(patientReportService.getPatientList(request.normalized()));
    }

    @Operation(summary = "환자 리포트 상세 조회",
        description = "UUID로 목록과 동일한 검사 정보 및 1장 이상의 PNG를 한 번에 조회합니다.")
    @GetMapping("/detail")
    public ResponseEntity<StaResponse<PatientDetailResponse>> getPatientDetail(
        @Parameter(description = "환자 리포트 Sample ID (UUID)") @RequestParam String sampleId) {
        var request = new PatientDetailSearchRequest(sampleId).normalized();
        var response = success(patientReportService.getPatientDetail(request));
        return ResponseEntity.status(response.getStatusCode()).cacheControl(CacheControl.noStore())
            .header("X-Content-Type-Options", "nosniff").body(response.getBody());
    }

    @Override
    protected MessageSource getMessageSource() {
        return messageSource;
    }
}
