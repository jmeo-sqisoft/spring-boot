package com.stagora.global.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.assertThat;

class StringUtilTest {
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n", "\u2003"})
    void recognizesMissingAndWhitespaceOnlyValues(String value) {
        assertThat(StringUtil.isBlank(value)).isTrue();
        assertThat(StringUtil.snakeToCamel(value)).isEqualTo(value);
    }

    @ParameterizedTest
    @CsvSource({"HOSPITAL_ID,hospitalId", "nation_region_no,nationRegionNo",
        "hospital,hospital", "AREA__CODE_,areaCode"})
    void convertsSnakeCaseToCamelCase(String input, String expected) {
        assertThat(StringUtil.isBlank(input)).isFalse();
        assertThat(StringUtil.snakeToCamel(input)).isEqualTo(expected);
    }

    @Test
    void sortsNumbersNumericallyWithoutChangingOriginalList() {
        var input = new ArrayList<>(List.of("10", "-2", "1.5", "2", "2"));

        assertThat(StringUtil.sortNumericList(input)).containsExactly("-2", "1.5", "2", "2", "10");
        assertThat(input).containsExactly("10", "-2", "1.5", "2", "2");
        assertThat(StringUtil.sortNumericList("10,-2,1.5,2,2")).isEqualTo("-2,1.5,2,2,10");
    }

    @Test
    void sortsNonNumericValuesLexicographically() {
        assertThat(StringUtil.sortNumericList(List.of("z", "b", "a")))
            .containsExactly("a", "b", "z");
    }

    @Test
    void sortsStringsWithoutChangingOriginalList() {
        var input = new ArrayList<>(List.of("10", "2", "1"));

        assertThat(StringUtil.sortStrList(input)).containsExactly("1", "10", "2");
        assertThat(input).containsExactly("10", "2", "1");
    }

    @Test
    void handlesMissingAndEmptyLists() {
        assertThat(StringUtil.sortNumericList((String) null)).isNull();
        assertThat(StringUtil.sortNumericList("")).isEmpty();
        assertThat(StringUtil.sortNumericList((List<String>) null)).isEmpty();
        assertThat(StringUtil.sortNumericList(List.of())).isEmpty();
        assertThat(StringUtil.sortStrList(null)).isEmpty();
        assertThat(StringUtil.sortStrList(List.of())).isEmpty();
    }
}
