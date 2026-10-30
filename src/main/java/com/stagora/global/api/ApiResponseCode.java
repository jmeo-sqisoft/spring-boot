package com.stagora.global.api;

import java.util.Arrays;

import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import com.stagora.global.constants.Messages;

import lombok.Getter;

/**
 * API 응답 코드와 HTTP 상태, 기본 다국어 메시지 키를 관리한다.
 */
@Getter
public enum ApiResponseCode {
    // 정상 처리
    S0000(HttpStatus.OK, "S0000", "success.default"),

    // 잘못된 요청
    E00400(HttpStatus.BAD_REQUEST, "E00400", Messages.KEY_VALIDATION_FAILED),
    // 인증 실패
    E00401(HttpStatus.UNAUTHORIZED, "E00401", "error.auth.unauthorized"),
    // 접근 권한 없음
    E00403(HttpStatus.FORBIDDEN, "E00403", "error.auth.forbidden"),
    // 요청한 리소스 없음
    E00404(HttpStatus.NOT_FOUND, "E00404", "error.notFound"),
    // 요청 횟수 제한 초과
    E00429(HttpStatus.TOO_MANY_REQUESTS, "E00429", "error.rate_limit.exceeded"),
    // 내부 시스템 오류
    E00500(HttpStatus.INTERNAL_SERVER_ERROR, "E00500", "error.internal"),

    // 이미 존재하는 데이터
    E00409(HttpStatus.CONFLICT, "E00409", "error.conflict.already_exists"),
    // 로그인 실패
    E00410(HttpStatus.UNAUTHORIZED, "E00410", "error.auth.login_failed"),
    // 허용되지 않은 IP
    E00411(HttpStatus.FORBIDDEN, "E00411", "error.auth.ip_not_allowed"),
    // Secure Code 서명 불일치 또는 만료
    E00412(HttpStatus.UNAUTHORIZED, "E00412", "error.auth.secure_code_invalid"),
    // IP 등록 한도 초과
    E00413(HttpStatus.UNAUTHORIZED, "E00413", "error.auth.ip_limit_exceeded"),
    // 지원하지 않는 접속 환경
    E00414(HttpStatus.UNAUTHORIZED, "E00414", "error.auth.unsupported_environment"),
    // 병원을 찾을 수 없음
    E00417(HttpStatus.BAD_REQUEST, "E00417", "error.business.hospital.not_found"),
    // 인증 번호 요청 내역 없음
    E00420(HttpStatus.BAD_REQUEST, "E00420", "error.auth.verification_request_not_found"),
    // 인증 번호 만료
    E00421(HttpStatus.BAD_REQUEST, "E00421", "error.auth.verification_expired"),
    // 인증 번호 불일치
    E00422(HttpStatus.BAD_REQUEST, "E00422", "error.auth.verification_mismatch"),
    // 등록된 이메일 없음
    E00423(HttpStatus.BAD_REQUEST, "E00423", "error.auth.email_not_registered"),
    // 이메일 템플릿 없음
    E00501(HttpStatus.INTERNAL_SERVER_ERROR, "E00501", "error.internal.email_template_not_found"),
    // 이메일 발송 실패
    E00502(HttpStatus.INTERNAL_SERVER_ERROR, "E00502", "error.internal.email_send_failed"),
    // 토큰 생성 실패
    E00503(HttpStatus.INTERNAL_SERVER_ERROR, "E00503", "error.internal.token_creation_failed");

    // 응답에 사용할 HTTP 상태
    private final HttpStatus httpStatus;
    // 응답 본문의 code 값
    private final String code;
    // MessageSource에서 메시지를 조회할 때 사용하는 기본 키
    private final String defaultMessageKey;

    ApiResponseCode(HttpStatus httpStatus, String code, String defaultMessageKey) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.defaultMessageKey = defaultMessageKey;
    }

    /**
     * 대소문자 구분 없이 응답 코드를 찾고, 일치하는 코드가 없으면 null을 반환한다.
     */
    public static ApiResponseCode fromErrorCode(String errorCode) {
        if (!StringUtils.hasText(errorCode)) {
            return null;
        }

        return Arrays.stream(ApiResponseCode.values())
            .filter(apiCode -> apiCode.code.equalsIgnoreCase(errorCode))
            .findFirst()
            .orElse(null);
    }

    public int getStatusCode() {
        return httpStatus.value();
    }
}
