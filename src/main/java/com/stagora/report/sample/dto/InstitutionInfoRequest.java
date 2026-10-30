package com.stagora.report.sample.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "기관정보 조회 Request")
public class InstitutionInfoRequest {
    @Schema(description = "검사실 ID", example = "CL0001")
    private String clabId;

    @Schema(description = "병원 ID", example = "019bf94a-1e58-773c-9d46-ae8318e6bcba")
    private String hospitalId;
}
