package com.stagora.reports.dto;

import java.util.UUID;

import com.stagora.global.api.ApiResponseCode;
import com.stagora.global.exception.ApiException;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 환자 리포트 단건 조회에 사용하는 Sample ID이다.
 */
@Schema(description = "환자 리포트 상세 조회 조건")
public record PatientDetailSearchRequest(
    @Schema(description = "조회할 Sample ID (UUID)", requiredMode = Schema.RequiredMode.REQUIRED,
        example = "01a01ce1-5a44-7416-943e-598621a158a2")
    String sampleId
) {
    /**
     * UUID를 표준 문자열로 정규화하며, 잘못된 값은 HTTP 400으로 처리한다.
     */
    public PatientDetailSearchRequest normalized() {
        if (sampleId == null) {
            throw new ApiException(ApiResponseCode.E40001);
        }
        String value = sampleId.trim();
        try {
            String normalized = UUID.fromString(value).toString();
            if (!normalized.equalsIgnoreCase(value)) {
                throw new ApiException(ApiResponseCode.E40001);
            }
            return new PatientDetailSearchRequest(normalized);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(ApiResponseCode.E40001);
        }
    }
}
