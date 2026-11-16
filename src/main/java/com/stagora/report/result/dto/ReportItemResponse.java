package com.stagora.report.result.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Sample ID별 리포트 조회 결과")
public record ReportItemResponse(
    @Schema(description = "입력한 검체 UUID. 미조회 항목에도 유지",
        example = "01a01ce1-5d69-7d03-99c9-669409908df7")
    String sampleId,
    @Schema(description = "리포트 조회 성공 여부. false이면 선택·미리보기·출력 불가", example = "true")
    boolean reportAvailable,
    @Schema(description = "검사 정보와 출력 이력. 미조회 시 null")
    ReportSearchResponse report
) {
}
