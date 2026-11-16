package com.stagora.global.exception;

import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.assertThat;

import com.stagora.global.api.ApiResponseCode;

class ApiExceptionTest {
    private static final String CUSTOM_MESSAGE_KEY = "error.custom.invalid";
    private static final String CUSTOM_ERROR_CODE = "E42201";

    @ParameterizedTest
    @MethodSource("explicitFailures")
    void preservesOptionalDataAndCauseForExplicitErrors(ApiException exception, Object data, Throwable cause) {
        assertThat(exception.getStatusCode()).isEqualTo(422);
        assertThat(exception.getErrorCode()).isEqualTo(CUSTOM_ERROR_CODE);
        assertThat(exception.getMessage()).isEqualTo(CUSTOM_MESSAGE_KEY);
        assertThat(exception.getMessageKey()).isEqualTo(CUSTOM_MESSAGE_KEY);
        assertThat(exception.getData()).isSameAs(data);
        assertThat(exception.getCause()).isSameAs(cause);
    }

    @Test
    void customMessageWithoutCausePreservesResponseCodeAndData() {
        var data = Map.of("field", "sampleId");
        var exception = new ApiException(ApiResponseCode.E40001, CUSTOM_MESSAGE_KEY, data);
        assertThat(exception.getStatusCode()).isEqualTo(400);
        assertThat(exception.getErrorCode()).isEqualTo("E40001");
        assertThat(exception.getMessage()).isEqualTo(CUSTOM_MESSAGE_KEY);
        assertThat(exception.getMessageKey()).isEqualTo(CUSTOM_MESSAGE_KEY);
        assertThat(exception.getData()).isSameAs(data);
        assertThat(exception.getCause()).isNull();
    }

    private static Stream<Arguments> explicitFailures() {
        var data = Map.of("field", "sampleId");
        var cause = new IllegalArgumentException("Invalid input");
        return Stream.of(
            Arguments.of(new ApiException(422, CUSTOM_ERROR_CODE, CUSTOM_MESSAGE_KEY), null, null),
            Arguments.of(new ApiException(422, CUSTOM_ERROR_CODE, CUSTOM_MESSAGE_KEY, data), data, null),
            Arguments.of(new ApiException(422, CUSTOM_ERROR_CODE, CUSTOM_MESSAGE_KEY, cause), null, cause));
    }

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

        var exception = new ApiException(ApiResponseCode.E40001, "error.validation.hospitalId.invalid", data, cause);

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
