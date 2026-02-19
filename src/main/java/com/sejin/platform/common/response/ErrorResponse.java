package com.sejin.platform.common.response;

import java.time.LocalDateTime;
import java.util.List;

/*
 * 프로젝트 전체에서 "실패 응답"을 하나의 형식으로 통일하기 위한 클래스
 *
 * 컨트롤러나 서비스에서 예외가 발생하면
 * GlobalExceptionHandler가 이 객체를 만들어서
 * JSON 형태로 내려준다.
 */
public class ErrorResponse {

    // 항상 실패니까 false로 고정
    private final boolean success;
    // 에러 종류 구분용 코드 (예: VALIDATION_ERROR, INTERNAL_ERROR)
    private final String code;
    // 사용자에게 보여줄 메시지
    private final String message;
    // 필드 단위 에러 목록 (유효성 검증 실패 시 사용)
    private final List<FieldError> errors;
    // 에러 발생 시각
    private final LocalDateTime timestamp;

    /*
     * 생성자는 외부에서 직접 못 쓰게 private으로 막아둠
     * 대신 아래 static of() 메서드를 통해서만 생성하도록 설계
     */
    private ErrorResponse(String code, String message, List<FieldError> errors) {
        this.success = false; // 실패 응답이므로 무조건 false
        this.code = code;
        this.message = message;
        this.errors = errors;
        this.timestamp = LocalDateTime.now(); // 응답 생성 시각 저장
    }

    // 필드 에러가 없는 일반 실패 응답 생성
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, null);
    }

    // 필드 에러가 포함된 실패 응답 생성
    public static ErrorResponse of(String code, String message, List<FieldError> errors) {
        return new ErrorResponse(code, message, errors);
    }

    // 유효성 검증처럼 필드 단위 에러가 있을 때 사용하는 내부 클래스
    public static class FieldError {

        // 어떤 필드에서 에러가 났는지
        private final String field;
        // 왜 에러가 났는지
        private final String reason;

        public FieldError(String field, String reason) {
            this.field = field;
            this.reason = reason;
        }

        public String getField() { return field; }
        public String getReason() { return reason; }
    }

    // JSON 변환을 위해 필요한 getter들
    public boolean isSuccess() { return success; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
    public List<FieldError> getErrors() { return errors; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
