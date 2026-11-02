package com.stagora.global.exception;

import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import static org.assertj.core.api.Assertions.assertThat;

import com.stagora.global.api.ApiResponseCode;

class ApiExceptionHandlerTest {
    private final StaticMessageSource messages = new StaticMessageSource();
    private final ApiExceptionHandler handler = new ApiExceptionHandler(messages);

    @Test
    void localizesMessageAndPreservesStatusCodeAndData() {
        messages.addMessage("error.validation.failed", Locale.KOREAN, "입력값 검증에 실패했습니다.");
        var data = Map.of("field", "clabId");
        var exception = new ApiException(ApiResponseCode.E40001, null, data);

        var response = handler.handle(exception, Locale.KOREAN);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("E40001");
        assertThat(response.getBody().message()).isEqualTo("입력값 검증에 실패했습니다.");
        assertThat(response.getBody().data()).isSameAs(data);
    }

    @Test
    void usesMessageKeyWhenTranslationIsMissing() {
        var response = handler.handle(new ApiException(ApiResponseCode.E40402), Locale.ENGLISH);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("E40402");
        assertThat(response.getBody().message()).isEqualTo("error.business.hospital.not_found");
        assertThat(response.getBody().data()).isNull();
    }
}
