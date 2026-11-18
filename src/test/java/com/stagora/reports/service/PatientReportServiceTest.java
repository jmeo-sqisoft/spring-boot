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

import com.stagora.reports.dto.PatientItemResponse;
import com.stagora.reports.dto.PatientListSearchRequest;
import com.stagora.reports.dto.PatientListSummaryResponse;
import com.stagora.reports.dto.PatientSearchConverter;
import com.stagora.reports.mapper.PatientReportMapper;
import com.stagora.reports.model.PatientListItem;
import com.stagora.reports.model.PatientListStatistics;

class PatientReportServiceTest {
    private static final String FIRST_SAMPLE_ID = "d1bad87f41a03fe91c0733a132de1e919b9c72e3dd8884005fffe79578f002ad";
    private static final String SECOND_SAMPLE_ID = "e5e024838ed6cbb8a89c1008877a2e4a9e6e52a19e9379b76af9339f0f14cdd2";
    private final PatientReportMapper mapper = mock(PatientReportMapper.class);
    private final PatientReportService service = new PatientReportService(mapper,
        Mappers.getMapper(PatientSearchConverter.class));

    @Test
    void returnsPaginationAndReportFieldsWithoutCollapsingRows() {
        var request = new PatientListSearchRequest("TEST_CLASSIFICATION", null, null, null, null, 1, 2).normalized();
        var date = LocalDate.of(2026, 11, 16);
        var first = new PatientListItem("c2eb2821-5623-3b5d-8272-19971c8b04c2", FIRST_SAMPLE_ID, "COMPREHENSIVE_PANEL",
            "HPV_STI", "Human Papillomavirus Infections + Sexually Transmitted Infections",
            "RTI", "HPV + STI Comprehensive Panel", date, 10, date, 0, null);
        var second = new PatientListItem("dfbffab0-8ce6-2ec4-57a6-c2cf7f55e76c", SECOND_SAMPLE_ID,
            "COMPREHENSIVE_PANEL", "RP", "Respiratory Tract Infections", "ABI", "Respiratory Comprehensive Panel",
            date.minusDays(1), 4, date.minusDays(1), 2, LocalDateTime.of(2026, 11, 15, 0, 0));
        when(mapper.getPatientListStatistics(request)).thenReturn(new PatientListStatistics(5, 4, 0, 0));
        when(mapper.getPatientList(request)).thenReturn(List.of(first, second));

        var response = service.getPatientList(request);

        assertThat(response.totalCount()).isEqualTo(5);
        assertThat(response.totalPages()).isEqualTo(3);
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.summary()).isNull();
        assertThat(response.items()).hasSize(2).extracting(PatientItemResponse::reportId)
            .containsExactly("c2eb2821-5623-3b5d-8272-19971c8b04c2", "dfbffab0-8ce6-2ec4-57a6-c2cf7f55e76c");
        assertThat(response.items()).extracting(PatientItemResponse::sampleId)
            .containsExactly(FIRST_SAMPLE_ID, SECOND_SAMPLE_ID);
        assertThat(response.items().get(0)).usingRecursiveComparison().isEqualTo(first);
        assertThat(response.items().get(1)).usingRecursiveComparison().isEqualTo(second);
        verify(mapper).getPatientListStatistics(request);
        verify(mapper).getPatientList(request);
        verifyNoMoreInteractions(mapper);
    }

    @Test
    void returnsZeroPagesAndSkipsRowsQueryWhenNoReportsMatch() {
        var request = new PatientListSearchRequest("TEST_CLASSIFICATION", null, "COMPREHENSIVE_PANEL", "GI",
            "TODAY", 1, 10).normalized();
        when(mapper.getPatientListStatistics(request)).thenReturn(new PatientListStatistics(0, 0, 0, 0));
        var response = service.getPatientList(request);
        assertThat(response.totalCount()).isZero();
        assertThat(response.totalPages()).isZero();
        assertThat(response.items()).isEmpty();
        verify(mapper).getPatientListStatistics(request);
        verifyNoMoreInteractions(mapper);
    }

    @Test
    void preservesTotalForOutOfRangePageAndRoundsPartialLastPageUp() {
        var request = new PatientListSearchRequest("TEST_CLASSIFICATION", null, null, null,
            null, 15, 3).normalized();
        when(mapper.getPatientListStatistics(request)).thenReturn(new PatientListStatistics(5, 4, 0, 0));
        when(mapper.getPatientList(request)).thenReturn(List.of());
        var response = service.getPatientList(request);
        assertThat(response.totalCount()).isEqualTo(5);
        assertThat(response.totalPages()).isEqualTo(2);
        assertThat(response.page()).isEqualTo(15);
        assertThat(response.items()).isEmpty();
        verify(mapper).getPatientList(request);
    }

    private PatientListSearchRequest request(int page) {
        return new PatientListSearchRequest("TEST_CLASSIFICATION", null, null, null, null, page, 10).normalized();
    }

    @Test
    void statisticsFailureIsNotReturnedAsEmptySearchResults() {
        var request = request(1);
        var failure = new DataAccessResourceFailureException("Statistics query unavailable");
        when(mapper.getPatientListStatistics(request)).thenThrow(failure);

        assertThatThrownBy(() -> service.getPatientList(request)).isSameAs(failure);
        verify(mapper).getPatientListStatistics(request);
        verifyNoMoreInteractions(mapper);
    }

    @Test
    void summaryCountsUniqueInputAndAllMatchesEvenForEmptyPage() {
        var request = new PatientListSearchRequest("SAMPLE_ID",
            List.of(FIRST_SAMPLE_ID, FIRST_SAMPLE_ID, SECOND_SAMPLE_ID, "S26-1015-9999"),
            null, null, "TODAY", 20, 1).normalized();
        when(mapper.getPatientListStatistics(request)).thenReturn(new PatientListStatistics(1, 1, 1, 1));
        when(mapper.getPatientList(request)).thenReturn(List.of());

        var response = service.getPatientList(request);

        assertThat(response.totalCount()).isEqualTo(1);
        assertThat(response.totalPages()).isEqualTo(1);
        assertThat(response.items()).isEmpty();
        assertThat(response.summary()).isEqualTo(new PatientListSummaryResponse(3, 1, 1, 1, 1));
        verify(mapper).getPatientListStatistics(request);
        verify(mapper).getPatientList(request);
        verifyNoMoreInteractions(mapper);
    }

    @ParameterizedTest
    @CsvSource({"S26-1015-9999,ALL_TIME,0,1",
        SECOND_SAMPLE_ID + ",TODAY,1,0"})
    void distinguishesNoResultsFromNotFoundWhenNoReportsMatch(String sampleId, String period,
        long noResults, long notFound) {
        var request = new PatientListSearchRequest("SAMPLE_ID", List.of(sampleId), null, null,
            period, null, null).normalized();
        when(mapper.getPatientListStatistics(request)).thenReturn(new PatientListStatistics(0, 0, noResults, notFound));

        var response = service.getPatientList(request);

        assertThat(response.totalCount()).isZero();
        assertThat(response.summary()).isEqualTo(new PatientListSummaryResponse(1, 0, 0, noResults, notFound));
        assertThat(response.items()).isEmpty();
        verify(mapper).getPatientListStatistics(request);
        verifyNoMoreInteractions(mapper);
    }
}
