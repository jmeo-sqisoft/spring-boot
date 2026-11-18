package com.stagora.reports.dto;

import com.stagora.global.api.ApiResponseCode;
import com.stagora.global.exception.ApiException;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 환자 리포트 단건 조회에 사용하는 Sample ID이다.
 */
@Schema(description = "환자 리포트 상세 조회 조건")
public record PatientDetailSearchRequest(
    @Schema(description = "조회할 Sample ID", requiredMode = Schema.RequiredMode.REQUIRED,
        example = "d1bad87f41a03fe91c0733a132de1e919b9c72e3dd8884005fffe79578f002ad")
    String sampleId
) {
    /**
     * 앞뒤 공백을 제거하고 목록 조회와 동일한 Sample ID 형식을 검증한다.
     */
    public PatientDetailSearchRequest normalized() {
        if (sampleId == null) {
            throw new ApiException(ApiResponseCode.E40001);
        }
        String value = sampleId.trim();
        if (!value.matches("[A-Za-z0-9][A-Za-z0-9-]{0,63}")) {
            throw new ApiException(ApiResponseCode.E40001);
        }
        return new PatientDetailSearchRequest(value);
    }
}
