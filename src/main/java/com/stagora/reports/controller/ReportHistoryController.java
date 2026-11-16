package com.stagora.reports.controller;

import java.util.List;

import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stagora.global.api.AbstractApiController;
import com.stagora.global.api.StaResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Report History", description = "리포트 이력 API")
@RestController
@RequestMapping("/apis/reports/v1/histories")
@RequiredArgsConstructor
public class ReportHistoryController extends AbstractApiController {
    private final MessageSource messageSource;

    @Operation(summary = "리포트 이력 목록 조회", description = "서비스 연결 전이며 빈 목록을 반환합니다.")
    @GetMapping("/list")
    public ResponseEntity<StaResponse<List<Object>>> getReportHistoryList() {
        return success(List.of());
    }

    @Operation(summary = "리포트 이력 상세 조회", description = "서비스 연결 전이며 data는 null입니다.")
    @GetMapping("/detail")
    public ResponseEntity<StaResponse<Object>> getReportHistorydetail(
        @Parameter(description = "리포트 이력 ID") @RequestParam String sampleId) {
        return success(null);
    }

    @Override
    protected MessageSource getMessageSource() {
        return messageSource;
    }
}
