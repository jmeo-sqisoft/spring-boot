package com.stagora.global.constants;

public final class Messages {
	public static final String DEFAULT_GRANULARITY = "week";
	/**
	 * MessageSource 키. 성공 응답 기본 메시지. doc/public/spec/architecture/04-i18n-and-exception.md
	 */
	public static final String KEY_SUCCESS_DEFAULT = "success.default";
	/**
	 * MessageSource 키. Bean Validation @NotBlank 등에서 {key} 참조
	 */
	public static final String KEY_VALIDATION_PARAM_REQUIRED = "error.validation.param.required";
	public static final String ERROR_VALIDATION_PARAM_REQUIRED = "{" + KEY_VALIDATION_PARAM_REQUIRED + "}";
	/**
	 * validation 실패 시 메시지 키. ApiException messageKey, MessageSource
	 */
	public static final String KEY_VALIDATION_FAILED = "error.validation.failed";
	public static final String ERROR_VALIDATION_UUID_INVALID = "{error.validation.uuid.invalid}";
	public static final String ERROR_VALIDATION_DISEASE_GRP_ID_INVALID = "{error.validation.diseaseGrpId.invalid}";
	/**
	 * period 형식 오류 메시지 키. ApiException messageKey, Bean Validation
	 */
	public static final String KEY_VALIDATION_PERIOD_INVALID = "error.validation.period.invalid";
	public static final String ERROR_VALIDATION_PERIOD_INVALID = "{" + KEY_VALIDATION_PERIOD_INVALID + "}";
	public static final String KEY_VALIDATION_NUMBER_INVALID = "error.validation.number.invalid";
	public static final String ERROR_VALIDATION_NUMBER_INVALID = "{" + KEY_VALIDATION_NUMBER_INVALID + "}";
	public static final String KEY_VALIDATION_NUMBER_LIST_INVALID = "error.validation.number_list.invalid";
	public static final String ERROR_VALIDATION_NUMBER_LIST_INVALID = "{" + KEY_VALIDATION_NUMBER_LIST_INVALID + "}";
	public static final String ERROR_VALIDATION_GRANULARITY_INVALID = "{error.validation.granularity.invalid}";
	public static final String ERROR_VALIDATION_PARAM_POSITIVE = "{error.validation.param.positive}";
	public static final String ERROR_VALIDATION_YN_INVALID = "{error.validation.yn.invalid}";
	public static final String ERROR_VALIDATION_COUNTRY_CODE_INVALID = "{error.validation.countryCode.invalid}";
	public static final String ERROR_VALIDATION_EXPT_YM_INVALID = "{error.validation.exptYm.invalid}";
	public static final String ERROR_VALIDATION_EXPT_DT_INVALID = "{error.validation.exptDt.invalid}";
	public static final String ERROR_VALIDATION_TARGETS_INVALID = "{error.validation.targets.invalid}";

	private Messages() {}
}
