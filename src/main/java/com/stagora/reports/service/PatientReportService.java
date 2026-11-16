package com.stagora.reports.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.stagora.global.api.ApiResponseCode;
import com.stagora.global.exception.ApiException;
import com.stagora.reports.dto.PatientDetailResponse;
import com.stagora.reports.dto.PatientDetailSearchRequest;
import com.stagora.reports.dto.PatientItemResponse;
import com.stagora.reports.dto.PatientListResponse;
import com.stagora.reports.dto.PatientListSearchRequest;
import com.stagora.reports.dto.PatientPageImageResponse;
import com.stagora.reports.dto.PatientSearchConverter;
import com.stagora.reports.mapper.PatientReportMapper;
import com.stagora.reports.model.PatientPageContent;
import com.stagora.reports.model.PatientSearch;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PatientReportService {
    private final PatientReportMapper patientReportMapper;
    private final PatientSearchConverter patientSearchConverter;

    /**
     * 목록과 동일한 검사 정보와 전체 PNG를 단건 응답으로 묶는다.
     * 검사 정보 또는 이미지가 없으면 HTTP 404이며, 조회로 출력 이력을 변경하지 않는다.
     */
    public PatientDetailResponse getPatientDetail(PatientDetailSearchRequest request) {
        PatientSearch report = patientReportMapper.getPatientDetail(request);
        if (report == null) {
            throw new ApiException(ApiResponseCode.E40401);
        }
        List<PatientPageContent> files =
            patientReportMapper.getPatientPageContents(request.sampleId());
        if (files.isEmpty()) {
            throw new ApiException(ApiResponseCode.E40401);
        }
        List<PatientPageImageResponse> pages = files.stream()
            .sorted(Comparator.comparingInt(PatientPageContent::page))
            .map(file -> new PatientPageImageResponse(file.page(), "image/png", file.contents()))
            .toList();
        return new PatientDetailResponse(patientSearchConverter.toResponse(report), pages);
    }

    /**
     * 탭별 검색 조건으로 전체 건수와 현재 페이지를 조회한다.
     * 같은 Sample ID의 여러 리포트를 각각 반환하며 미조회 ID의 빈 행은 추가하지 않는다.
     */
    public PatientListResponse getPatientList(PatientListSearchRequest request) {
        long total = patientReportMapper.getPatientListCount(request);
        List<PatientItemResponse> items = total == 0 ? List.of()
            : patientReportMapper.getPatientList(request).stream()
                .map(patientSearchConverter::toItemResponse).toList();
        long pages = total / request.size() + (total % request.size() == 0 ? 0 : 1);
        return new PatientListResponse(total, request.page(), request.size(), pages, items);
    }
}
