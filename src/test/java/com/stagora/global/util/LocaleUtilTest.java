package com.stagora.global.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class LocaleUtilTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "fr", "ja"})
    void fallsBackToEnglishForMissingOrUnsupportedLanguage(String language) {
        assertThat(LocaleUtil.getLocationLang(language)).isEqualTo("en");
    }

    @Test
    void normalizesSupportedLanguages() {
        assertThat(LocaleUtil.getLocationLang(" KO ")).isEqualTo("ko");
        assertThat(LocaleUtil.getLocationLang(" EN ")).isEqualTo("en");
    }

    @Test
    void extractsLanguageAndDefaultsMissingLocale() {
        assertThat(LocaleUtil.getLanguage(Locale.KOREA)).isEqualTo("ko");
        assertThat(LocaleUtil.getLanguage(Locale.US)).isEqualTo("en");
        assertThat(LocaleUtil.getLanguage(null)).isEqualTo("en");
        assertThat(LocaleUtil.getLanguage(Locale.ROOT)).isEqualTo("en");
    }
}
