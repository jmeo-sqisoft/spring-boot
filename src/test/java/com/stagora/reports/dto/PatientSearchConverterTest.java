package com.stagora.reports.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import static org.assertj.core.api.Assertions.assertThat;

import com.stagora.reports.model.PatientSearch;

class PatientSearchConverterTest {
    private static final String SAMPLE_ID = "e5e024838ed6cbb8a89c1008877a2e4a9e6e52a19e9379b76af9339f0f14cdd2";
    private final PatientSearchConverter converter = Mappers.getMapper(PatientSearchConverter.class);

    @Test
    void preservesAllReportFieldsAndPrintHistory() {
        var report = new PatientSearch(SAMPLE_ID, "ABI", "Respiratory Comprehensive Panel",
            LocalDate.of(2026, 11, 15), 4, LocalDate.of(2026, 11, 15), 2, LocalDateTime.of(2026, 11, 15, 0, 0));

        var response = converter.toResponse(report);

        assertThat(response).isEqualTo(new PatientSearchResponse(
            SAMPLE_ID, "ABI", "Respiratory Comprehensive Panel", LocalDate.of(2026, 11, 15),
            4, LocalDate.of(2026, 11, 15), 2, LocalDateTime.of(2026, 11, 15, 0, 0)));
    }

    @Test
    void returnsNullForMissingReport() {
        assertThat(converter.toResponse(null)).isNull();
        assertThat(converter.toItemResponse(null)).isNull();
    }
}
