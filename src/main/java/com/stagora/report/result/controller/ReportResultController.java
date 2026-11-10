package com.stagora.report.result.controller;

import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stagora.global.api.AbstractApiController;
import com.stagora.global.api.ApiResponseCode;
import com.stagora.global.api.StaResponse;
import com.stagora.global.exception.ApiException;
import com.stagora.report.result.dto.ReportSearchRequest;
import com.stagora.report.result.dto.ReportSearchResultResponse;
import com.stagora.report.result.service.ReportResultService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Report", description = "리포트 조회 API")
@RestController
@RequestMapping("/apis/report/v1/reports")
@RequiredArgsConstructor
public class ReportResultController extends AbstractApiController {
    private final MessageSource messageSource;
    private final ReportResultService reportResultService;

    @Operation(summary = "리포트 조회",
        description = "Sample ID는 필수이며, 입력 순서대로 리포트와 미조회 항목을 반환합니다.")
    @PostMapping("/search")
    public ResponseEntity<StaResponse<ReportSearchResultResponse>> search(
        @RequestBody(required = false) ReportSearchRequest request) {
        if (request == null) {
            throw new ApiException(ApiResponseCode.E40001);
        }
        return success(reportResultService.search(request.normalized()));
    }

    @Override
    protected MessageSource getMessageSource() {
        return messageSource;
    }
}
