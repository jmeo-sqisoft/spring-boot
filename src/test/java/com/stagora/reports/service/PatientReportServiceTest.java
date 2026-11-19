package com.stagora.reports.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mapstruct.factory.Mappers;
import org.springframework.dao.DataAccessResourceFailureException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.stagora.reports.dto.PatientClassificationSummaryResponse;
import com.stagora.reports.dto.PatientItemResponse;
import com.stagora.reports.dto.PatientListSearchRequest;
import com.stagora.reports.dto.PatientSampleSummaryResponse;
import com.stagora.reports.dto.PatientSearchConverter;
import com.stagora.reports.mapper.PatientReportMapper;
import com.stagora.reports.model.PatientListItem;
import com.stagora.reports.model.PatientListStatistics;

class PatientReportServiceTest {
    private static final String FIRST_SAMPLE_ID = "d1bad87f41a03fe91c0733a132de1e919b9c72e3dd8884005fffe79578f002ad";
    private static final String SECOND_SAMPLE_ID = "e5e024838ed6cbb8a89c1008877a2e4a9e6e52a19e9379b76af9339f0f14cdd2";
    private static final PatientSampleSummaryResponse ZERO_SAMPLE = new PatientSampleSummaryResponse(0, 0, 0, 0, 0);
    private static final PatientClassificationSummaryResponse ZERO_CLASSIFICATION =
        new PatientClassificationSummaryResponse(0, 0, 0, 0, 0);
    private static final PatientClassificationSummaryResponse CLASSIFICATION_SUMMARY =
        new PatientClassificationSummaryResponse(5, 4, 2, 2, 1);
    private final PatientReportMapper mapper = mock(PatientReportMapper.class);
    private final PatientReportService service = new PatientReportService(mapper,
        Mappers.getMapper(PatientSearchConverter.class));

    @Test
    void returnsAllMatchingReportsWithoutCollapsingDuplicateSampleIds() {
        var request = new PatientListSearchRequest("TEST_CLASSIFICATION", null, null, null, null).normalized();
        var reports = comprehensiveReports();
        when(mapper.getPatientListStatistics(request)).thenReturn(new PatientListStatistics(5, 4, 0, 0, 2, 2, 1));
        when(mapper.getPatientList(request)).thenReturn(reports);

        var response = service.getPatientList(request);

        assertThat(response.totalCount()).isEqualTo(5);
        assertThat(response.sampleSummary()).isEqualTo(ZERO_SAMPLE);
        assertThat(response.classificationSummary()).isEqualTo(CLASSIFICATION_SUMMARY);
        assertThat(response.items()).hasSize(5).zipSatisfy(reports,
            (actual, expected) -> assertThat(actual).usingRecursiveComparison().isEqualTo(expected));
        verify(mapper).getPatientListStatistics(request);
        verify(mapper).getPatientList(request);
        verifyNoMoreInteractions(mapper);
    }

    @Test
    void returnsZeroCountsAndSkipsRowsQueryWhenNoReportsMatch() {
        var request = new PatientListSearchRequest("TEST_CLASSIFICATION", null, "COMPREHENSIVE_PANEL", "GI", "TODAY")
            .normalized();
        when(mapper.getPatientListStatistics(request)).thenReturn(new PatientListStatistics(0, 0, 0, 0, 0, 0, 0));
        var response = service.getPatientList(request);
        assertThat(response.totalCount()).isZero();
        assertThat(response.classificationSummary()).isEqualTo(ZERO_CLASSIFICATION);
        assertThat(response.sampleSummary()).isEqualTo(ZERO_SAMPLE);
        assertThat(response.items()).isEmpty();
        verify(mapper).getPatientListStatistics(request);
        verifyNoMoreInteractions(mapper);
    }

    @Test
    void diseaseFilterSummaryIncludesZeroCountsForOtherPanelsAndInactiveSampleSummary() {
        var request = new PatientListSearchRequest("TEST_CLASSIFICATION", null, null, "RP", null).normalized();
        when(mapper.getPatientListStatistics(request)).thenReturn(new PatientListStatistics(2, 2, 0, 0, 0, 2, 0));
        var reports = comprehensiveReports().stream().filter(report -> "ABI".equals(report.panelCode())).toList();
        when(mapper.getPatientList(request)).thenReturn(reports);

        var response = service.getPatientList(request);

        assertThat(response.totalCount()).isEqualTo(2);
        assertThat(response.items()).hasSize(2).zipSatisfy(reports,
            (actual, expected) -> assertThat(actual).usingRecursiveComparison().isEqualTo(expected));
        assertThat(response.sampleSummary()).isEqualTo(ZERO_SAMPLE);
        assertThat(response.classificationSummary())
            .isEqualTo(new PatientClassificationSummaryResponse(2, 2, 0, 2, 0));
        verify(mapper).getPatientListStatistics(request);
        verify(mapper).getPatientList(request);
        verifyNoMoreInteractions(mapper);
    }

    private List<PatientListItem> comprehensiveReports() {
        var date = LocalDate.of(2026, 11, 16);
        var first = new PatientListItem("c2eb2821-5623-3b5d-8272-19971c8b04c2", FIRST_SAMPLE_ID, "COMPREHENSIVE_PANEL",
            "HPV_STI", "Human Papillomavirus Infections + Sexually Transmitted Infections",
            "RTI", "HPV + STI Comprehensive Panel", date, 10, date, 0, null);
        var second = new PatientListItem("dfbffab0-8ce6-2ec4-57a6-c2cf7f55e76c", SECOND_SAMPLE_ID,
            "COMPREHENSIVE_PANEL", "RP", "Respiratory Tract Infections", "ABI", "Respiratory Comprehensive Panel",
            date.minusDays(1), 4, date.minusDays(1), 2, LocalDateTime.of(2026, 11, 15, 0, 0));
        var third = new PatientListItem("6085db41-848b-e6eb-82c2-6abd7b0d0c03",
            "0b83f2fc4bf07a1cb34ff69c96758aec9e6d0d168d8e069db2e69d4872068909",
            "COMPREHENSIVE_PANEL", "GI", "Gastrointestinal Infections", "GTI", "Gastrointestinal Comprehensive Panel",
            date.minusDays(2), 10, date.minusDays(2), 0, null);
        String sharedSampleId = "b009a5f7798b5fdc35935933cfbd7e01930a7aeb63b907db5876ff4e12f48953";
        var fourth = new PatientListItem("cb4ececd-dcb4-517c-a0bc-ddafd23813b9", sharedSampleId,
            "COMPREHENSIVE_PANEL", "HPV_STI", "Human Papillomavirus Infections + Sexually Transmitted Infections",
            "RTI", "HPV + STI Comprehensive Panel", date.minusDays(3), 4, date.minusDays(3), 2,
            LocalDateTime.of(2026, 11, 15, 0, 0));
        var fifth = new PatientListItem("e5925354-8ffc-b090-ecc0-cd0944afd27f", sharedSampleId,
            "COMPREHENSIVE_PANEL", "RP", "Respiratory Tract Infections", "ABI", "Respiratory Comprehensive Panel",
            date.minusDays(4), 10, date.minusDays(4), 1, LocalDateTime.of(2026, 11, 15, 0, 0));
        return List.of(first, second, third, fourth, fifth);
    }

    @Test
    void statisticsFailureIsNotReturnedAsEmptySearchResults() {
        var request = new PatientListSearchRequest("TEST_CLASSIFICATION", null, null, null, null).normalized();
        var failure = new DataAccessResourceFailureException("Statistics query unavailable");
        when(mapper.getPatientListStatistics(request)).thenThrow(failure);

        assertThatThrownBy(() -> service.getPatientList(request)).isSameAs(failure);
        verify(mapper).getPatientListStatistics(request);
        verifyNoMoreInteractions(mapper);
    }

    @Test
    void summaryCountsUniqueInputAndAllReturnedMatches() {
        var request = new PatientListSearchRequest("SAMPLE_ID",
            List.of(FIRST_SAMPLE_ID, FIRST_SAMPLE_ID, SECOND_SAMPLE_ID, "S26-1015-9999"), null, null, "TODAY")
            .normalized();
        when(mapper.getPatientListStatistics(request)).thenReturn(new PatientListStatistics(1, 1, 1, 1, 1, 0, 0));
        when(mapper.getPatientList(request)).thenReturn(List.of(comprehensiveReports().get(0)));

        var response = service.getPatientList(request);

        assertThat(response.totalCount()).isEqualTo(1);
        assertThat(response.items()).singleElement().extracting(PatientItemResponse::sampleId)
            .isEqualTo(FIRST_SAMPLE_ID);
        assertThat(response.sampleSummary()).isEqualTo(new PatientSampleSummaryResponse(3, 1, 1, 1, 1));
        assertThat(response.classificationSummary()).isEqualTo(ZERO_CLASSIFICATION);
        verify(mapper).getPatientListStatistics(request);
        verify(mapper).getPatientList(request);
        verifyNoMoreInteractions(mapper);
    }

    @ParameterizedTest
    @CsvSource({"S26-1015-9999,ALL_TIME,0,1",
        SECOND_SAMPLE_ID + ",TODAY,1,0"})
    void distinguishesNoResultsFromNotFoundWhenNoReportsMatch(String sampleId, String period,
        long noResults, long notFound) {
        var request = new PatientListSearchRequest("SAMPLE_ID", List.of(sampleId), null, null, period).normalized();
        when(mapper.getPatientListStatistics(request))
            .thenReturn(new PatientListStatistics(0, 0, noResults, notFound, 0, 0, 0));

        var response = service.getPatientList(request);

        assertThat(response.totalCount()).isZero();
        assertThat(response.sampleSummary()).isEqualTo(new PatientSampleSummaryResponse(1, 0, 0, noResults, notFound));
        assertThat(response.items()).isEmpty();
        verify(mapper).getPatientListStatistics(request);
        verifyNoMoreInteractions(mapper);
    }
}
