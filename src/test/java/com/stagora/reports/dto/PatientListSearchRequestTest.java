package com.stagora.reports.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
        assertThat(request.testClassification()).isEqualTo("ALL");
        assertThat(request.disease()).isEqualTo("ALL");
        assertThat(request.testDate()).isEqualTo("ALL_TIME");
        assertThat(request.page()).isEqualTo(1);
        assertThat(request.size()).isEqualTo(10);
        assertThat(request.offset()).isZero();
    }

    @ParameterizedTest
    @CsvSource({"ALL,ALL", "COMPREHENSIVE_PANEL,ALL", "COMPREHENSIVE_PANEL,HPV_STI",
        "COMPREHENSIVE_PANEL,RP", "COMPREHENSIVE_PANEL,GI", "OTHER_PANELS,ALL",
        "OTHER_PANELS,HPV", "OTHER_PANELS,STI", "OTHER_PANELS,RP", "OTHER_PANELS,GI", "OTHER_PANELS,OTHERS"})
    void acceptsClassificationSpecificDiseasesWithoutSampleIds(String classification, String disease) {
        var input = new PatientListSearchRequest(" test_classification ", null,
            classification.toLowerCase(Locale.ROOT), disease, null, 2, 10);
        var request = input.normalized();
        assertThat(request.sampleIds()).isEmpty();
        assertThat(request.testClassification()).isEqualTo(classification);
        assertThat(request.disease()).isEqualTo(disease);
        assertThat(request.offset()).isEqualTo(10);
    }

    @ParameterizedTest
    @CsvSource({"ALL,HPV", "COMPREHENSIVE_PANEL,HPV", "COMPREHENSIVE_PANEL,OTHERS",
        "OTHER_PANELS,HPV_STI", "OTHER_PANELS,UNKNOWN"})
    void rejectsDiseaseOutsideSelectedClassification(String classification, String disease) {
        var input = new PatientListSearchRequest("TEST_CLASSIFICATION", null,
            classification, disease, null, null, null);
        assertThatThrownBy(input::normalized).isInstanceOf(ApiException.class);
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
    @ValueSource(ints = {1, 50})
    void acceptsUniqueIdCountBoundaries(int count) {
        var ids = IntStream.range(0, count).mapToObj(index -> "S" + index).toList();
        var input = new PatientListSearchRequest(null, ids, null, null, null, null, null);
        assertThat(input.normalized().sampleIds()).hasSize(count).containsExactlyElementsOf(ids);
    }

    @Test
    void rejectsMoreThanFiftyUniqueIdsButAllowsDuplicatesBeforeCounting() {
        var ids = IntStream.range(0, 51).mapToObj(index -> "S" + index).toList();
        var input = new PatientListSearchRequest(null, ids, null, null, null, null, null);
        assertThatThrownBy(input::normalized).isInstanceOf(ApiException.class);
        var duplicates = new PatientListSearchRequest(null,
            IntStream.range(0, 100).mapToObj(index -> "S" + index % 50).toList(), null, null, null, null, null);
        assertThat(duplicates.normalized().sampleIds()).hasSize(50);
    }

    @Test
    void largePageOffsetDoesNotOverflowInteger() {
        var input = new PatientListSearchRequest("TEST_CLASSIFICATION", null, null, null, null, Integer.MAX_VALUE, 100);
        assertThat(input.normalized().offset()).isEqualTo(214748364600L);
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
    void rejectsOverlongIdAndDiseaseFilterInSampleTab() {
        var overlong = new PatientListSearchRequest(null, List.of("A".repeat(65)),
            null, null, null, null, null);
        var mixed = new PatientListSearchRequest(null, List.of("S1"), null, "RP", null, null, null);
        assertThatThrownBy(overlong::normalized).isInstanceOf(ApiException.class);
        assertThatThrownBy(mixed::normalized).isInstanceOf(ApiException.class);
    }

    @Test
    void classificationTabAcceptsExplicitEmptySampleIds() {
        var input = new PatientListSearchRequest("TEST_CLASSIFICATION", List.of(), null, null, null, null, null);
        var normalized = input.normalized();
        assertThat(normalized.sampleIds()).isEmpty();
        assertThat(normalized.searchType()).isEqualTo("TEST_CLASSIFICATION");
        assertThat(normalized.testClassification()).isEqualTo("ALL");
        assertThat(normalized.disease()).isEqualTo("ALL");
    }
}
