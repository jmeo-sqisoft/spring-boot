package com.seegene.local.base.enums;

import com.seegene.local.base.constants.Messages;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.util.Arrays;

/**
 * API 응답 코드 enum.
 * <p>SSP 형식 응답의 code 필드값과 HTTP 상태, 다국어 메시지 키를 한 곳에서 관리한다.</p>
 * <p>사용 예: {@code throw new ApiException(ApiResponseCode.E00400, Messages.KEY_VALIDATION_PERIOD_INVALID);}</p>
 * <p>출처: doc/public/how-to/06-response-codes-reference.md, doc/public/spec/contracts/endpoints-v1.md §2.3</p>
 */
@Getter
public enum ApiResponseCode {
	// === 성공 ===
	/**
	 * 200 OK. 정상 처리. 응답 body의 code 필드에 "S0000" 사용
	 */
	S0000(HttpStatus.OK, "S0000", "success.default"),

	// === 공통 에러 (Statistics / IAM 공통 사용) ===
	/**
	 * 400 Bad Request. 잘못된 요청(파라미터 누락, 형식 오류, period 해석 실패 등)
	 */
	E00400(HttpStatus.BAD_REQUEST, "E00400", Messages.KEY_VALIDATION_FAILED),
	/**
	 * 401 Unauthorized. 유효하지 않은 토큰, 로그인 실패, 토큰 만료 등
	 */
	E00401(HttpStatus.UNAUTHORIZED, "E00401", "error.auth.unauthorized"),
	/**
	 * 403 Forbidden. API 권한 없음, IP 접근 제한 등
	 */
	E00403(HttpStatus.FORBIDDEN, "E00403", "error.auth.forbidden"),
	/**
	 * 404 Not Found. 요청한 리소스 없음(NoHandlerFoundException 등)
	 */
	E00404(HttpStatus.NOT_FOUND, "E00404", "error.notFound"),
	/**
	 * 429 Too Many Requests. Rate Limiting 초과
	 */
	E00429(HttpStatus.TOO_MANY_REQUESTS, "E00429", "error.rate_limit.exceeded"),
	/**
	 * 500 Internal Server Error. 내부 시스템 오류
	 */
	E00500(HttpStatus.INTERNAL_SERVER_ERROR, "E00500", "error.internal"),

	// === IAM·Auth 전용 (로그인, 회원가입, Secure Link 등) ===
	/**
	 * 409 Conflict. 이미 존재하는 데이터(중복 가입 등)
	 */
	E00409(HttpStatus.CONFLICT, "E00409", "error.conflict.already_exists"),
	/**
	 * 401. 로그인 실패(ID/PW 불일치)
	 */
	E00410(HttpStatus.UNAUTHORIZED, "E00410", "error.auth.login_failed"),
	/**
	 * 403. 허용되지 않은 IP
	 */
	E00411(HttpStatus.FORBIDDEN, "E00411", "error.auth.ip_not_allowed"),
	/**
	 * 401. Secure Code 서명 불일치 또는 만료
	 */
	E00412(HttpStatus.UNAUTHORIZED, "E00412", "error.auth.secure_code_invalid"),
	/**
	 * 401. IP 등록 한도 초과
	 */
	E00413(HttpStatus.UNAUTHORIZED, "E00413", "error.auth.ip_limit_exceeded"),
	/**
	 * 401. 지원하지 않는 접속 환경
	 */
	E00414(HttpStatus.UNAUTHORIZED, "E00414", "error.auth.unsupported_environment"),
	/**
	 * 400. 병원을 찾을 수 없음
	 */
	E00417(HttpStatus.BAD_REQUEST, "E00417", "error.business.hospital.not_found"),
	/**
	 * 400. 인증 번호 요청 내역 없음
	 */
	E00420(HttpStatus.BAD_REQUEST, "E00420", "error.auth.verification_request_not_found"),
	/**
	 * 400. 인증 번호 만료
	 */
	E00421(HttpStatus.BAD_REQUEST, "E00421", "error.auth.verification_expired"),
	/**
	 * 400. 인증 번호 불일치
	 */
	E00422(HttpStatus.BAD_REQUEST, "E00422", "error.auth.verification_mismatch"),
	/**
	 * 400. 등록된 이메일 없음
	 */
	E00423(HttpStatus.BAD_REQUEST, "E00423", "error.auth.email_not_registered"),
	/**
	 * 500. 이메일 템플릿 없음
	 */
	E00501(HttpStatus.INTERNAL_SERVER_ERROR, "E00501", "error.internal.email_template_not_found"),
	/**
	 * 500. 이메일 발송 실패
	 */
	E00502(HttpStatus.INTERNAL_SERVER_ERROR, "E00502", "error.internal.email_send_failed"),
	/**
	 * 500. 토큰 생성 실패
	 */
	E00503(HttpStatus.INTERNAL_SERVER_ERROR, "E00503", "error.internal.token_creation_failed");

	/**
	 * HTTP 상태 코드 (200, 400, 401 등)
	 * -- GETTER --
	 * HTTP 상태 enum 반환
	 */
	private final HttpStatus httpStatus;
	/**
	 * SSP 응답 body의 code 필드값 (예: "E00400")
	 * -- GETTER --
	 * SSP 응답 code 문자열 반환 (예: "E00400")
	 */
	private final String code;
	/**
	 * MessageSource용 키. messages_ko.properties 등에서 다국어 메시지 조회
	 * -- GETTER --
	 * 다국어 메시지 키 반환. ApiException 생성 시 messageKey 오버라이드 가능
	 */
	private final String defaultMessageKey;

	ApiResponseCode(HttpStatus httpStatus, String code, String defaultMessageKey) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.defaultMessageKey = defaultMessageKey;
	}

	public static ApiResponseCode fromErrorCode(String errorCode) {
		if (!StringUtils.hasText(errorCode)) {
			return null;
		}

		return Arrays.stream(ApiResponseCode.values())
			.filter(apiCode -> apiCode.code.equalsIgnoreCase(errorCode))
			.findFirst()
			.orElse(null);
	}

	/**
	 * HTTP 상태 코드 숫자 반환 (예: 400)
	 */
	public int getStatusCode() {
		return httpStatus.value();
	}
}
