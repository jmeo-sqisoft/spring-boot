package com.stagora.reports.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stagora.global.exception.ApiException;

class PatientDetailSearchRequestTest {
    @Test
    void trimsAndNormalizesUuidWithoutChangingOriginalRequest() {
        var request = new PatientDetailSearchRequest(" 01A01CE1-5A44-7416-943E-598621A158A2 ");

        var normalized = request.normalized();

        assertThat(normalized.sampleId()).isEqualTo("01a01ce1-5a44-7416-943e-598621a158a2");
        assertThat(request.sampleId()).isEqualTo(" 01A01CE1-5A44-7416-943E-598621A158A2 ");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "S1", "1-1-1-1-1"})
    void rejectsMissingOrNonCanonicalUuid(String id) {
        var request = new PatientDetailSearchRequest(id);

        assertThatThrownBy(request::normalized).isInstanceOfSatisfying(ApiException.class, exception -> {
            assertThat(exception.getStatusCode()).isEqualTo(400);
            assertThat(exception.getErrorCode()).isEqualTo("E40001");
        });
    }
}
