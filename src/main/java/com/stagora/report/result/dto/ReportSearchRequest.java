package com.stagora.report.result.dto;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.stagora.global.api.ApiResponseCode;
import com.stagora.global.exception.ApiException;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Sample ID·검사 패널·최근 기간으로 리포트를 조회하는 요청")
public record ReportSearchRequest(
    @Schema(description = "조회할 Sample ID 배열. 중복 제거 후 1~50건 필수",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "[\"S26-1015-0231\", \"S26-1015-0232\", \"S26-1015-0235\", \"S26-1015-0240\"]")
    List<String> sampleIds,
    @Schema(description = "검사 패널. 생략하면 전체", example = "ALL", defaultValue = "ALL",
        allowableValues = {"ALL", "RTI", "ABI", "GTI"})
    String panelCode,
    @Schema(description = "최근 조회 개월 수. 1~12, 생략하면 최근 3개월", example = "3",
        minimum = "1", maximum = "12", defaultValue = "3")
    Integer periodMonths
) {
    /**
     * 입력을 분리·정규화하고 최초 입력 순서로 중복을 제거한다.
     * 전체 패널은 null로 변환하고, 조회 개월 수가 없으면 3을 적용한다.
     * 잘못된 조건은 HTTP 400 예외로 처리하며, 실제 조회 기간은 SQL에서 계산한다.
     * 원본 요청은 변경하지 않고 정규화한 새 요청을 반환한다.
     */
    public ReportSearchRequest normalized() {
        if (sampleIds == null || sampleIds.isEmpty()) {
            throw new ApiException(ApiResponseCode.E40001);
        }
        Set<String> uniqueIds = new LinkedHashSet<>();
        for (String input : sampleIds) {
            if (input == null) {
                throw new ApiException(ApiResponseCode.E40001);
            }
            addSampleIds(input, uniqueIds);
        }
        if (uniqueIds.isEmpty()) {
            throw new ApiException(ApiResponseCode.E40001);
        }
        String panel = panelCode == null ? "ALL" : panelCode.trim().toUpperCase(Locale.ROOT);
        if (!Set.of("ALL", "RTI", "ABI", "GTI").contains(panel)) {
            throw new ApiException(ApiResponseCode.E40001);
        }
        int months = periodMonths == null ? 3 : periodMonths;
        if (months < 1 || months > 12) {
            throw new ApiException(ApiResponseCode.E40001);
        }
        return new ReportSearchRequest(List.copyOf(uniqueIds),
            "ALL".equals(panel) ? null : panel, months);
    }

    /**
     * 배열 원소를 구분자로 나누고 빈 구간을 제외해 중복 제거 목록에 추가한다.
     * ID는 영문·숫자로 시작하며 영문·숫자·하이픈 1~64자만 허용한다.
     */
    private static void addSampleIds(String input, Set<String> uniqueIds) {
        for (String token : input.split("[\\r\\n,\\t]+")) {
            String id = token.trim();
            if (id.isEmpty()) {
                continue;
            }
            if (!id.matches("[A-Za-z0-9][A-Za-z0-9-]{0,63}")) {
                throw new ApiException(ApiResponseCode.E40001);
            }
            uniqueIds.add(id);
            if (uniqueIds.size() > 50) {
                throw new ApiException(ApiResponseCode.E40001);
            }
        }
    }
}
