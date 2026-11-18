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
    void trimsSampleIdWithoutChangingCaseOrOriginalRequest() {
        var request = new PatientDetailSearchRequest(" AbC-123 ");

        var normalized = request.normalized();

        assertThat(normalized.sampleId()).isEqualTo("AbC-123");
        assertThat(request.sampleId()).isEqualTo(" AbC-123 ");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "bad id", "id_1", "-id", "id,other"})
    void rejectsMissingOrMalformedSampleId(String id) {
        var request = new PatientDetailSearchRequest(id);

        assertThatThrownBy(request::normalized).isInstanceOfSatisfying(ApiException.class, exception -> {
            assertThat(exception.getStatusCode()).isEqualTo(400);
            assertThat(exception.getErrorCode()).isEqualTo("E40001");
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 64})
    void acceptsSampleIdLengthBoundaries(int length) {
        String id = "a".repeat(length);
        var request = new PatientDetailSearchRequest(id);
        assertThat(request.normalized().sampleId()).isEqualTo(id);
    }

    @Test
    void rejectsSampleIdLongerThanSixtyFourCharacters() {
        var request = new PatientDetailSearchRequest("a".repeat(65));
        assertThatThrownBy(request::normalized).isInstanceOfSatisfying(ApiException.class, exception -> {
            assertThat(exception.getStatusCode()).isEqualTo(400);
            assertThat(exception.getErrorCode()).isEqualTo("E40001");
        });
    }
}
