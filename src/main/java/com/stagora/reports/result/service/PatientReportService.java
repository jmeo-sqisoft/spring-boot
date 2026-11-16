package com.stagora.reports.result.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.stagora.global.api.ApiResponseCode;
import com.stagora.global.exception.ApiException;
import com.stagora.reports.result.dto.PatientDetailResponse;
import com.stagora.reports.result.dto.PatientDetailSearchRequest;
import com.stagora.reports.result.dto.PatientItemResponse;
import com.stagora.reports.result.dto.PatientListResponse;
import com.stagora.reports.result.dto.PatientListSearchRequest;
import com.stagora.reports.result.dto.PatientPageImageResponse;
import com.stagora.reports.result.dto.PatientSearchConverter;
import com.stagora.reports.result.mapper.PatientReportMapper;
import com.stagora.reports.result.model.PatientPageContent;
import com.stagora.reports.result.model.PatientSearch;

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
     * 필수 Sample ID와 검사 패널·기간 조건으로 리포트를 조회한다.
     * 입력 순서를 유지하고, 조건에 맞는 리포트가 없는 ID도 결과에 포함한다.
     */
    public PatientListResponse getPatientList(PatientListSearchRequest request) {
        List<PatientSearch> rows = patientReportMapper.getPatientList(request);
        // DB 조회 순서와 관계없이 입력 ID 순서로 결과를 구성할 수 있도록 매핑한다.
        Map<String, PatientSearch> reports = rows.stream()
            .collect(Collectors.toMap(PatientSearch::sampleId, Function.identity()));
        List<PatientItemResponse> items = request.sampleIds().stream().map(sampleId -> {
            PatientSearch report = reports.get(sampleId);
            // 미조회 ID는 reportAvailable=false, report=null로 반환한다.
            return new PatientItemResponse(sampleId, report != null,
                patientSearchConverter.toResponse(report));
        }).toList();
        int foundCount = (int)items.stream().filter(PatientItemResponse::reportAvailable).count();
        return new PatientListResponse(
            items.size(), foundCount, items.size() - foundCount, items);
    }
}
