package com.stagora.global.api;

import java.util.Arrays;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseCodeTest {
    @ParameterizedTest
    @EnumSource(ApiResponseCode.class)
    void resolvesCodesIgnoringCaseAndProvidesTranslatedMessages(ApiResponseCode code) {
        assertThat(ApiResponseCode.fromErrorCode(code.getCode().toLowerCase(Locale.ROOT)))
            .isSameAs(code);
        var messages = new ResourceBundleMessageSource();
        messages.setBasename("messages/messages");
        messages.setDefaultEncoding("UTF-8");
        messages.setFallbackToSystemLocale(false);
        for (Locale locale : new Locale[] {Locale.ENGLISH, Locale.KOREAN}) {
            assertThat(messages.getMessage(code.getDefaultMessageKey(), null, locale))
                .isNotBlank().isNotEqualTo(code.getDefaultMessageKey());
        }
    }

    @ParameterizedTest
    @EnumSource(value = ApiResponseCode.class, names = "S0000", mode = EnumSource.Mode.EXCLUDE)
    void errorCodeContainsHttpStatusAndPositiveSequence(ApiResponseCode code) {
        assertThat(code.getCode()).matches("E" + code.getStatusCode() + "[0-9]{2}");
        assertThat(Integer.parseInt(code.getCode().substring(4))).isPositive();
    }

    @Test
    void keepsResponseCodesUnique() {
        assertThat(Arrays.stream(ApiResponseCode.values()).map(ApiResponseCode::getCode).toList())
            .doesNotHaveDuplicates();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "UNKNOWN", "E99999", " E40001 "})
    void returnsNullForUnknownOrUntrimmedCodes(String code) {
        assertThat(ApiResponseCode.fromErrorCode(code)).isNull();
    }
}
