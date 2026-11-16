package com.stagora.reports.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import static org.assertj.core.api.Assertions.assertThat;

import com.stagora.reports.model.PatientSearch;

class PatientSearchConverterTest {
    private final PatientSearchConverter converter = Mappers.getMapper(PatientSearchConverter.class);

    @Test
    void preservesAllReportFieldsAndPrintHistory() {
        var report = new PatientSearch("01a01ce1-5a44-7416-943e-598621a158a2", "ABI", "호흡기 감염 종합",
            LocalDate.of(2026, 10, 15), 5, LocalDate.of(2026, 10, 15), 1, LocalDateTime.of(2026, 10, 16, 0, 0));

        var response = converter.toResponse(report);

        assertThat(response).isEqualTo(new PatientSearchResponse(
            "01a01ce1-5a44-7416-943e-598621a158a2", "ABI", "호흡기 감염 종합",
            LocalDate.of(2026, 10, 15), 5, LocalDate.of(2026, 10, 15), 1, LocalDateTime.of(2026, 10, 16, 0, 0)));
    }

    @Test
    void returnsNullForMissingReport() {
        assertThat(converter.toResponse(null)).isNull();
        assertThat(converter.toItemResponse(null)).isNull();
    }
}
