package com.stagora.reports.dto;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.stagora.global.api.ApiResponseCode;
import com.stagora.global.exception.ApiException;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "환자 리포트 목록의 검색 탭·검사일·페이지 조건")
public record PatientListSearchRequest(
    @Schema(description = "검색 탭", defaultValue = "SAMPLE_ID", allowableValues = {"SAMPLE_ID", "TEST_CLASSIFICATION"})
    String searchType,
    @Schema(description = "Sample ID 탭에서 필수. 구분자 분리·중복 제거 후 1~50건", example = "[\"S26-1015-0231\"]")
    List<String> sampleIds,
    @Schema(description = "검사 분류", defaultValue = "ALL",
        allowableValues = {"ALL", "COMPREHENSIVE_PANEL", "OTHER_PANELS"})
    String testClassification,
    @Schema(description = "질환. 종합 패널은 HPV_STI·RP·GI, 기타 패널은 HPV·STI·RP·GI·OTHERS", defaultValue = "ALL")
    String disease,
    @Schema(description = "검사일 기간. 오늘 포함, 최근 7일은 오늘부터 6일 전까지", defaultValue = "ALL_TIME",
        allowableValues = {"ALL_TIME", "TODAY", "LAST_7_DAYS", "LAST_1_MONTH"})
    String testDate,
    @Schema(description = "1부터 시작하는 페이지", defaultValue = "1", minimum = "1")
    Integer page,
    @Schema(description = "페이지당 건수 (1~100)", defaultValue = "10", minimum = "1", maximum = "100")
    Integer size
) {
    /**
     * 탭별 조건을 검증한다. 다른 탭의 조건이 섞이거나 분류에 맞지 않는 질환이면 거절한다.
     * Sample ID는 최초 입력 순서를 유지해 중복을 제거하며 기본 기간은 전체 기간이다.
     */
    public PatientListSearchRequest normalized() {
        String type = code(searchType, "SAMPLE_ID");
        String classification = code(testClassification, "ALL");
        String diseaseCode = code(disease, "ALL");
        String date = code(testDate, "ALL_TIME");
        int number = page == null ? 1 : page;
        int limit = size == null ? 10 : size;
        if (!Set.of("SAMPLE_ID", "TEST_CLASSIFICATION").contains(type)
            || !Set.of("ALL", "COMPREHENSIVE_PANEL", "OTHER_PANELS").contains(classification)
            || !Set.of("ALL_TIME", "TODAY", "LAST_7_DAYS", "LAST_1_MONTH").contains(date)
            || number < 1 || limit < 1 || limit > 100) {
            throw new ApiException(ApiResponseCode.E40001);
        }
        List<String> ids = List.of();
        if ("SAMPLE_ID".equals(type)) {
            if (!"ALL".equals(classification) || !"ALL".equals(diseaseCode)) {
                throw new ApiException(ApiResponseCode.E40001);
            }
            ids = normalizedIds();
        } else {
            if (sampleIds != null && !sampleIds.isEmpty()) {
                throw new ApiException(ApiResponseCode.E40001);
            }
            Set<String> diseases = switch (classification) {
                case "COMPREHENSIVE_PANEL" -> Set.of("ALL", "HPV_STI", "RP", "GI");
                case "OTHER_PANELS" -> Set.of("ALL", "HPV", "STI", "RP", "GI", "OTHERS");
                default -> Set.of("ALL");
            };
            if (!diseases.contains(diseaseCode)) {
                throw new ApiException(ApiResponseCode.E40001);
            }
        }
        return new PatientListSearchRequest(type, ids, classification, diseaseCode, date, number, limit);
    }

    /**
     * 큰 페이지 번호에서도 정수 오버플로가 발생하지 않도록 long으로 계산한다.
     */
    public long offset() {
        return ((long)page - 1) * size;
    }

    private List<String> normalizedIds() {
        if (sampleIds == null || sampleIds.isEmpty()) {
            throw new ApiException(ApiResponseCode.E40001);
        }
        Set<String> ids = new LinkedHashSet<>();
        for (String input : sampleIds) {
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
                if (ids.size() > 50) {
                    throw new ApiException(ApiResponseCode.E40001);
                }
            }
        }
        if (ids.isEmpty()) {
            throw new ApiException(ApiResponseCode.E40001);
        }
        return List.copyOf(ids);
    }

    private static String code(String value, String defaultValue) {
        return value == null ? defaultValue : value.trim().toUpperCase(Locale.ROOT);
    }
}
