package com.stagora.global.api;

import java.util.Arrays;

import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import com.stagora.global.constants.Messages;

import lombok.Getter;

/**
 * API 응답 코드와 HTTP 상태, 기본 다국어 메시지 키를 관리한다.
 * 오류 코드는 E + HTTP 상태 3자리 + 해당 상태의 순번 2자리로 구성한다.
 * 순번은 HTTP 상태별로 01부터 시작하며, 성공 코드는 S0000을 사용한다.
 */
@Getter
public enum ApiResponseCode {
    // 2xx: 정상 처리
    S0000(HttpStatus.OK, "S0000", "success.default"),

    // 400: 잘못된 요청과 입력 검증 실패
    // 잘못된 요청
    E40001(HttpStatus.BAD_REQUEST, "E40001", Messages.KEY_VALIDATION_FAILED),

    // 401: 인증 정보가 없거나 유효하지 않음
    // 인증 실패
    E40101(HttpStatus.UNAUTHORIZED, "E40101", "error.auth.unauthorized"),
    // 로그인 실패
    E40102(HttpStatus.UNAUTHORIZED, "E40102", "error.auth.login_failed"),
    // Secure Code 서명 불일치 또는 만료
    E40103(HttpStatus.UNAUTHORIZED, "E40103", "error.auth.secure_code_invalid"),
    // 인증 번호 만료
    E40104(HttpStatus.UNAUTHORIZED, "E40104", "error.auth.verification_expired"),
    // 인증 번호 불일치
    E40105(HttpStatus.UNAUTHORIZED, "E40105", "error.auth.verification_mismatch"),

    // 403: 접근 권한 또는 접속 정책에 따른 거부
    // 접근 권한 없음
    E40301(HttpStatus.FORBIDDEN, "E40301", "error.auth.forbidden"),
    // 허용되지 않은 IP
    E40302(HttpStatus.FORBIDDEN, "E40302", "error.auth.ip_not_allowed"),
    // IP 등록 한도 초과
    E40303(HttpStatus.FORBIDDEN, "E40303", "error.auth.ip_limit_exceeded"),
    // 지원하지 않는 접속 환경
    E40304(HttpStatus.FORBIDDEN, "E40304", "error.auth.unsupported_environment"),

    // 404: 요청한 조회 대상이 없음
    // 요청한 리소스 없음
    E40401(HttpStatus.NOT_FOUND, "E40401", "error.notFound"),
    // 병원을 찾을 수 없음
    E40402(HttpStatus.NOT_FOUND, "E40402", "error.business.hospital.not_found"),
    // 인증 번호 요청 내역 없음
    E40403(HttpStatus.NOT_FOUND, "E40403", "error.auth.verification_request_not_found"),
    // 등록된 이메일 없음
    E40404(HttpStatus.NOT_FOUND, "E40404", "error.auth.email_not_registered"),

    // 409: 현재 데이터 상태와 충돌
    // 이미 존재하는 데이터
    E40901(HttpStatus.CONFLICT, "E40901", "error.conflict.already_exists"),

    // 429: 요청 횟수 제한
    // 요청 횟수 제한 초과
    E42901(HttpStatus.TOO_MANY_REQUESTS, "E42901", "error.rate_limit.exceeded"),

    // 500: 서버 내부 처리 실패
    // 내부 시스템 오류
    E50001(HttpStatus.INTERNAL_SERVER_ERROR, "E50001", "error.internal"),
    // 이메일 템플릿 없음
    E50002(HttpStatus.INTERNAL_SERVER_ERROR, "E50002", "error.internal.email_template_not_found"),
    // 이메일 발송 실패
    E50003(HttpStatus.INTERNAL_SERVER_ERROR, "E50003", "error.internal.email_send_failed"),
    // 토큰 생성 실패
    E50004(HttpStatus.INTERNAL_SERVER_ERROR, "E50004", "error.internal.token_creation_failed");

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
     * 대소문자 구분 없이 응답 코드를 찾는다. 입력이 비어 있거나 일치하는 코드가 없으면
     * null을 반환하며, 코드 앞뒤의 공백을 제거하지는 않는다.
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

    /**
     * 응답 코드에 대응하는 HTTP 상태의 숫자 값을 반환한다.
     */
    public int getStatusCode() {
        return httpStatus.value();
    }
}
