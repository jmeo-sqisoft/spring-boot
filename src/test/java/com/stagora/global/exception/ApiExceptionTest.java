package com.stagora.global.exception;

import java.util.Map;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import com.stagora.global.api.ApiResponseCode;

class ApiExceptionTest {
    @Test
    void usesResponseCodeStatusAndDefaultMessageKey() {
        var exception = new ApiException(ApiResponseCode.E40001);

        assertThat(exception.getStatusCode()).isEqualTo(400);
        assertThat(exception.getErrorCode()).isEqualTo("E40001");
        assertThat(exception.getMessageKey()).isEqualTo("error.validation.failed");
        assertThat(exception.getMessage()).isEqualTo(exception.getMessageKey());
        assertThat(exception.getData()).isNull();
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void preservesCustomMessageDataAndCause() {
        var data = Map.of("field", "hospitalId");
        var cause = new IllegalArgumentException("Invalid identifier");

        var exception = new ApiException(ApiResponseCode.E40001,
            "error.validation.hospitalId.invalid", data, cause);

        assertThat(exception.getStatusCode()).isEqualTo(400);
        assertThat(exception.getErrorCode()).isEqualTo("E40001");
        assertThat(exception.getMessageKey()).isEqualTo("error.validation.hospitalId.invalid");
        assertThat(exception.getMessage()).isEqualTo(exception.getMessageKey());
        assertThat(exception.getData()).isSameAs(data);
        assertThat(exception.getCause()).isSameAs(cause);
    }

    @Test
    void retainsDefaultMessageWhenCauseIsProvided() {
        var cause = new IllegalStateException("Mail server unavailable");

        var exception = new ApiException(ApiResponseCode.E50003, cause);

        assertThat(exception.getStatusCode()).isEqualTo(500);
        assertThat(exception.getErrorCode()).isEqualTo("E50003");
        assertThat(exception.getMessageKey()).isEqualTo("error.internal.email_send_failed");
        assertThat(exception.getCause()).isSameAs(cause);
    }

    @Test
    void preservesExplicitErrorInformation() {
        var data = Map.of("requestId", "test-request");
        var cause = new IllegalStateException("Upstream unavailable");

        var exception = new ApiException(503, "CUSTOM", "error.unavailable", data, cause);

        assertThat(exception.getStatusCode()).isEqualTo(503);
        assertThat(exception.getErrorCode()).isEqualTo("CUSTOM");
        assertThat(exception.getMessage()).isEqualTo("error.unavailable");
        assertThat(exception.getMessageKey()).isEqualTo("error.unavailable");
        assertThat(exception.getData()).isSameAs(data);
        assertThat(exception.getCause()).isSameAs(cause);
    }
}
