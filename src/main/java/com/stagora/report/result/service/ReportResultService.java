package com.stagora.report.result.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.stagora.report.result.dto.ReportItemResponse;
import com.stagora.report.result.dto.ReportSearchConverter;
import com.stagora.report.result.dto.ReportSearchRequest;
import com.stagora.report.result.dto.ReportSearchResultResponse;
import com.stagora.report.result.mapper.ReportResultMapper;
import com.stagora.report.result.model.ReportSearch;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportResultService {
    private final ReportResultMapper reportResultMapper;
    private final ReportSearchConverter reportSearchConverter;

    /**
     * 필수 Sample ID와 검사 패널·기간 조건으로 리포트를 조회한다.
     * 입력 순서를 유지하고, 조건에 맞는 리포트가 없는 ID도 결과에 포함한다.
     */
    public ReportSearchResultResponse search(ReportSearchRequest request) {
        List<ReportSearch> rows = reportResultMapper.findReports(request);
        // DB 조회 순서와 관계없이 입력 ID 순서로 결과를 구성할 수 있도록 매핑한다.
        Map<String, ReportSearch> reports = rows.stream()
            .collect(Collectors.toMap(ReportSearch::sampleId, Function.identity()));
        List<ReportItemResponse> items = request.sampleIds().stream().map(sampleId -> {
            ReportSearch report = reports.get(sampleId);
            // 미조회 ID는 reportAvailable=false, report=null로 반환한다.
            return new ReportItemResponse(sampleId, report != null,
                reportSearchConverter.toResponse(report));
        }).toList();
        int foundCount = (int)items.stream().filter(ReportItemResponse::reportAvailable).count();
        return new ReportSearchResultResponse(
            items.size(), foundCount, items.size() - foundCount, items);
    }
}
