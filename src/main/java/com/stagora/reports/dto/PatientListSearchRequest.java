package com.stagora.reports.dto;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.stagora.global.api.ApiResponseCode;
import com.stagora.global.exception.ApiException;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "환자 리포트 목록의 검색 탭·검사 분류·질환·검사일 조건")
public record PatientListSearchRequest(
    @Schema(description = "검색 탭", defaultValue = "SAMPLE_ID", allowableValues = {"SAMPLE_ID", "TEST_CLASSIFICATION"})
    String searchType,
    @Schema(description = "Sample ID 탭에서 필수. 구분자 분리·중복 제거 후 1건 이상",
        example = "[\"d1bad87f41a03fe91c0733a132de1e919b9c72e3dd8884005fffe79578f002ad\"]")
    List<String> sampleIds,
    @Schema(description = "검사 분류", defaultValue = "COMPREHENSIVE_PANEL", allowableValues = {"COMPREHENSIVE_PANEL"})
    String testClassification,
    @Schema(description = "종합 패널 내 질환", defaultValue = "ALL", allowableValues = {"ALL", "HPV_STI", "RP", "GI"})
    String disease,
    @Schema(description = "검사일 기간. 오늘 포함, 최근 7일은 오늘부터 6일 전까지", defaultValue = "ALL_TIME",
        allowableValues = {"ALL_TIME", "TODAY", "LAST_7_DAYS", "LAST_1_MONTH"})
    String testDate
) {
    private static final Set<String> SEARCH_TYPES = Set.of("SAMPLE_ID", "TEST_CLASSIFICATION");
    private static final Set<String> TEST_CLASSIFICATIONS = Set.of("COMPREHENSIVE_PANEL");
    private static final Set<String> TEST_DATE_PERIODS = Set.of("ALL_TIME", "TODAY", "LAST_7_DAYS", "LAST_1_MONTH");
    private static final Set<String> DISEASES = Set.of("ALL", "HPV_STI", "RP", "GI");

    /**
     * 종합 패널 고정 조건과 질환·탭별 입력을 검증한다.
     * Sample ID는 최초 입력 순서를 유지해 중복을 제거하며 기본 기간은 전체 기간이다.
     */
    public PatientListSearchRequest normalized() {
        String type = code(searchType, "SAMPLE_ID");
        String classification = code(testClassification, "COMPREHENSIVE_PANEL");
        String diseaseCode = code(disease, "ALL");
        String date = code(testDate, "ALL_TIME");
        if (!SEARCH_TYPES.contains(type) || !TEST_CLASSIFICATIONS.contains(classification)
            || !DISEASES.contains(diseaseCode) || !TEST_DATE_PERIODS.contains(date)) {
            throw new ApiException(ApiResponseCode.E40001);
        }
        List<String> ids = List.of();
        if ("SAMPLE_ID".equals(type)) {
            ids = normalizedIds();
        } else if (sampleIds != null && !sampleIds.isEmpty()) {
            throw new ApiException(ApiResponseCode.E40001);
        }
        return new PatientListSearchRequest(type, ids, classification, diseaseCode, date);
    }

    private List<String> normalizedIds() {
        if (sampleIds == null || sampleIds.isEmpty()) {
            throw new ApiException(ApiResponseCode.E40001);
        }
        Set<String> ids = new LinkedHashSet<>();
        for (String input : sampleIds) {
            addNormalizedIds(input, ids);
        }
        if (ids.isEmpty()) {
            throw new ApiException(ApiResponseCode.E40001);
        }
        return List.copyOf(ids);
    }

    private static void addNormalizedIds(String input, Set<String> ids) {
        if (input == null) {
            throw new ApiException(ApiResponseCode.E40001);
        }
        for (String token : input.split("[\\r\\n,\\t]+")) {
            String id = token.trim();
            if (id.isEmpty()) {
                continue;
            }
            if (!id.matches("[A-Za-z0-9][A-Za-z0-9-]{0,63}")) {
                throw new ApiException(ApiResponseCode.E40001);
            }
            ids.add(id);
        }
    }

    private static String code(String value, String defaultValue) {
        return value == null ? defaultValue : value.trim().toUpperCase(Locale.ROOT);
    }
}
