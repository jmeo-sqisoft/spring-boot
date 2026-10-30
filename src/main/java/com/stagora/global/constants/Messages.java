package com.stagora.global.constants;

/**
 * 다국어 메시지 키와 입력 검증에서 참조하는 메시지 표현식을 정의한다.
 */
public final class Messages {
    // 집계 기간의 기본 단위
    public static final String DEFAULT_GRANULARITY = "week";
    // 성공 응답의 기본 메시지 키
    public static final String KEY_SUCCESS_DEFAULT = "success.default";
    // 필수 입력 검증 키와 검증 애너테이션에서 참조하는 {키} 형태의 표현식
    public static final String KEY_VALIDATION_PARAM_REQUIRED = "error.validation.param.required";
    public static final String ERROR_VALIDATION_PARAM_REQUIRED =
        "{" + KEY_VALIDATION_PARAM_REQUIRED + "}";
    // 입력 검증 실패 시 사용하는 기본 메시지 키
    public static final String KEY_VALIDATION_FAILED = "error.validation.failed";
    public static final String ERROR_VALIDATION_UUID_INVALID = "{error.validation.uuid.invalid}";
    public static final String ERROR_VALIDATION_DISEASE_GRP_ID_INVALID =
        "{error.validation.diseaseGrpId.invalid}";
    // 기간 형식 검증 키와 메시지 표현식
    public static final String KEY_VALIDATION_PERIOD_INVALID = "error.validation.period.invalid";
    public static final String ERROR_VALIDATION_PERIOD_INVALID =
        "{" + KEY_VALIDATION_PERIOD_INVALID + "}";
    public static final String KEY_VALIDATION_NUMBER_INVALID = "error.validation.number.invalid";
    public static final String ERROR_VALIDATION_NUMBER_INVALID =
        "{" + KEY_VALIDATION_NUMBER_INVALID + "}";
    public static final String KEY_VALIDATION_NUMBER_LIST_INVALID =
        "error.validation.number_list.invalid";
    public static final String ERROR_VALIDATION_NUMBER_LIST_INVALID =
        "{" + KEY_VALIDATION_NUMBER_LIST_INVALID + "}";
    public static final String ERROR_VALIDATION_GRANULARITY_INVALID =
        "{error.validation.granularity.invalid}";
    public static final String ERROR_VALIDATION_PARAM_POSITIVE =
        "{error.validation.param.positive}";
    public static final String ERROR_VALIDATION_YN_INVALID = "{error.validation.yn.invalid}";
    public static final String ERROR_VALIDATION_COUNTRY_CODE_INVALID =
        "{error.validation.countryCode.invalid}";
    public static final String ERROR_VALIDATION_EXPT_YM_INVALID =
        "{error.validation.exptYm.invalid}";
    public static final String ERROR_VALIDATION_EXPT_DT_INVALID =
        "{error.validation.exptDt.invalid}";
    public static final String ERROR_VALIDATION_TARGETS_INVALID =
        "{error.validation.targets.invalid}";

    private Messages() {}
}
