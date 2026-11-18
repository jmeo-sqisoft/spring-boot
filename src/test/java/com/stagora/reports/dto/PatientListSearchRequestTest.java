package com.stagora.reports.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stagora.global.exception.ApiException;

class PatientListSearchRequestTest {
    @Test
    void defaultsToSampleTabAllTimeAndFirstPageAndDeduplicatesIds() {
        var input = new PatientListSearchRequest(null, List.of(" S2,S1\r\nS2", "\tS3"), null, null, null, null, null);
        var request = input.normalized();

        assertThat(request.sampleIds()).containsExactly("S2", "S1", "S3");
        assertThat(request.searchType()).isEqualTo("SAMPLE_ID");
        assertThat(request.testClassification()).isEqualTo("COMPREHENSIVE_PANEL");
        assertThat(request.disease()).isEqualTo("ALL");
        assertThat(request.testDate()).isEqualTo("ALL_TIME");
        assertThat(request.page()).isEqualTo(1);
        assertThat(request.size()).isEqualTo(10);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALL", "HPV_STI", "RP", "GI"})
    void acceptsComprehensiveDiseasesForBothSearchTabs(String disease) {
        for (String type : List.of("SAMPLE_ID", "TEST_CLASSIFICATION")) {
            var input = new PatientListSearchRequest(type, "SAMPLE_ID".equals(type) ? List.of("S1") : null,
                " comprehensive_panel ", disease.toLowerCase(Locale.ROOT), null, 2, 10);
            var request = input.normalized();
            assertThat(request.testClassification()).isEqualTo("COMPREHENSIVE_PANEL");
            assertThat(request.disease()).isEqualTo(disease);
            assertThat(request.page()).isEqualTo(2);
            assertThat(request.size()).isEqualTo(10);
            if ("TEST_CLASSIFICATION".equals(type)) {
                assertThat(request.sampleIds()).isEmpty();
            } else {
                assertThat(request.sampleIds()).containsExactly("S1");
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALL", "OTHER_PANELS", "UNKNOWN", ""})
    void rejectsNonComprehensiveClassificationForBothTabs(String classification) {
        for (String type : List.of("SAMPLE_ID", "TEST_CLASSIFICATION")) {
            var input = new PatientListSearchRequest(type, "SAMPLE_ID".equals(type) ? List.of("S1") : null,
                classification, "ALL", null, null, null);
            assertThatThrownBy(input::normalized).isInstanceOfSatisfying(ApiException.class, exception -> {
                assertThat(exception.getStatusCode()).isEqualTo(400);
                assertThat(exception.getErrorCode()).isEqualTo("E40001");
            });
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"HPV", "STI", "OTHERS", "UNKNOWN", ""})
    void rejectsUnsupportedDiseaseForBothTabs(String disease) {
        for (String type : List.of("SAMPLE_ID", "TEST_CLASSIFICATION")) {
            var input = new PatientListSearchRequest(type, "SAMPLE_ID".equals(type) ? List.of("S1") : null,
                null, disease, null, null, null);
            assertThatThrownBy(input::normalized).isInstanceOfSatisfying(ApiException.class, exception -> {
                assertThat(exception.getStatusCode()).isEqualTo(400);
                assertThat(exception.getErrorCode()).isEqualTo("E40001");
            });
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALL_TIME", "TODAY", "LAST_7_DAYS", "LAST_1_MONTH"})
    void acceptsDatePeriodsForBothTabs(String period) {
        for (String type : List.of("SAMPLE_ID", "TEST_CLASSIFICATION")) {
            var input = new PatientListSearchRequest(type, "SAMPLE_ID".equals(type) ? List.of("S1") : null,
                null, null, period, null, null);
            assertThat(input.normalized().testDate()).isEqualTo(period);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 50, 51, 1000})
    void acceptsSampleIdsWithoutCountLimit(int count) {
        var ids = IntStream.range(0, count).mapToObj(index -> "S" + index).toList();
        var input = new PatientListSearchRequest(null, ids, null, null, null, null, null);
        assertThat(input.normalized().sampleIds()).hasSize(count).containsExactlyElementsOf(ids);
    }

    @Test
    void removesDuplicatesWithoutLimitingUniqueIdCount() {
        var ids = IntStream.range(0, 100).mapToObj(index -> "S" + index).toList();
        var duplicates = new PatientListSearchRequest(null,
            IntStream.range(0, 200).mapToObj(index -> "S" + index % 100).toList(), null, null, null, null, null);
        assertThat(duplicates.normalized().sampleIds()).containsExactlyElementsOf(ids);
    }

    @ParameterizedTest
    @CsvSource({"1,1", "2,100"})
    void acceptsPageSizeBoundaries(int page, int size) {
        var input = new PatientListSearchRequest(null, List.of("S1"), null, null, null, page, size);
        var request = input.normalized();
        assertThat(request.page()).isEqualTo(page);
        assertThat(request.size()).isEqualTo(size);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 64})
    void acceptsIdLengthBoundariesAndCopiesInputList(int length) {
        var ids = new ArrayList<>(List.of("A".repeat(length)));
        var input = new PatientListSearchRequest(null, ids, null, null, null, null, null);
        var normalized = input.normalized();
        ids.clear();
        assertThat(normalized.sampleIds()).containsExactly("A".repeat(length));
    }

    @Test
    void rejectsOverlongId() {
        var overlong = new PatientListSearchRequest(null, List.of("A".repeat(65)),
            null, null, null, null, null);
        assertThatThrownBy(overlong::normalized).isInstanceOf(ApiException.class);
    }

    @Test
    void classificationTabAcceptsExplicitEmptySampleIds() {
        var input = new PatientListSearchRequest("TEST_CLASSIFICATION", List.of(), null, null, null, null, null);
        var normalized = input.normalized();
        assertThat(normalized.sampleIds()).isEmpty();
        assertThat(normalized.searchType()).isEqualTo("TEST_CLASSIFICATION");
        assertThat(normalized.testClassification()).isEqualTo("COMPREHENSIVE_PANEL");
        assertThat(normalized.disease()).isEqualTo("ALL");
    }

    @ParameterizedTest
    @MethodSource("invalidSampleIds")
    void rejectsMissingOrMalformedSampleIdsWithValidationCode(List<String> ids) {
        var request = new PatientListSearchRequest(null, ids, null, null, null, null, null);

        assertThatThrownBy(request::normalized).isInstanceOfSatisfying(ApiException.class, exception -> {
            assertThat(exception.getStatusCode()).isEqualTo(400);
            assertThat(exception.getErrorCode()).isEqualTo("E40001");
        });
    }

    private static List<List<String>> invalidSampleIds() {
        return Arrays.asList(null, List.of(), Arrays.asList("S1", null), List.of(" ,\t\r\n "),
            List.of("S1", "bad id"), List.of("-S1"), List.of("S1_2"));
    }
}
