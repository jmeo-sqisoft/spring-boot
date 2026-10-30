package com.stagora.report.sample.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 컨트롤러에서 서비스와 Mapper로 전달하는 기관 조회 조건이다.
 * clabId와 hospitalId 중 하나 이상이 필요하며, 두 값이 있으면 병원 조회를 우선한다.
 */
@Getter
@Setter
@Schema(description = "기관 정보 조회 조건")
public class InstitutionInfoRequest {
    @Schema(description = "검사실 ID", example = "CL0001")
    private String clabId;

    @Schema(description = "병원 ID", example = "019bf94a-1e58-773c-9d46-ae8318e6bcba")
    private String hospitalId;
}
