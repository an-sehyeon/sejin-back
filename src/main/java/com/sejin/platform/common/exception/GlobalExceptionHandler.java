package com.sejin.platform.common.exception;

import com.sejin.platform.common.response.ErrorResponse;
import com.sejin.platform.common.response.ErrorResponse.FieldError;

import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * 프로젝트 전체에서 발생하는 예외를 한 곳에서 처리하는 클래스
 * 컨트롤러에서 예외가 발생하면 여기로 들어와서
 * 미리 정해둔 ErrorResponse 형식으로 통일해서 내려준다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 서버 내부에서 어떤 예외가 발생했는지 로그로 남기기 위한 Logger
    // 사용자에게는 내부 에러 내용을 그대로 노출하지 않고
    // 서버 로그로만 원인을 확인하기 위해 사용한다.
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    /*
     * 1. @Valid 붙은 객체 검증 실패 시 발생
     * 예: DTO에 @NotBlank, @Size 같은 검증 어노테이션이 붙어있고
     *     그 조건을 만족하지 못했을 때 발생하는 예외
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {

        // 어떤 필드가 어떤 이유로 실패했는지 하나씩 꺼내서
        // FieldError 형태로 변환한다.
        List<FieldError> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> 
                    // fe.getField() -> 필드명
                    // fe.getDefaultMessage() -> 검증 실패 메시지
                    new FieldError(fe.getField(), fe.getDefaultMessage())
                )
                .collect(Collectors.toList());

        // 공통 에러 응답 생성
        ErrorResponse body = ErrorResponse.of(
                "VALIDATION_ERROR",
                "입력값이 올바르지 않습니다.",
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }


    /*
     * 2. 폼 데이터나 쿼리 파라미터 바인딩 실패 시 발생
     * 예: 숫자여야 하는데 문자가 들어온 경우 등
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBind(BindException e) {

        List<FieldError> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe ->
                    new FieldError(fe.getField(), fe.getDefaultMessage())
                )
                .collect(Collectors.toList());

        ErrorResponse body = ErrorResponse.of(
                "BIND_ERROR",
                "요청 파라미터가 올바르지 않습니다.",
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }


    /*
     * 3. 파일 업로드 용량 초과 시 발생
     * application 설정에서 최대 용량을 초과하면 이 예외가 터진다.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUpload(MaxUploadSizeExceededException e) {

        ErrorResponse body = ErrorResponse.of(
                "FILE_TOO_LARGE",
                "업로드 파일 용량이 너무 큽니다."
        );

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(body);
    }


    /*
     * 4. @RequestParam, @PathVariable 등에 붙은 검증 실패
     * 예: @NotBlank, @Min 같은 검증이 파라미터에 직접 붙은 경우
     *     @Validated가 클래스에 있어야 동작한다.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e){

        // ConstraintViolationException 안에는
        // 어떤 파라미터가 어떤 규칙을 어겼는지 정보가 들어있다.
        // 그걸 프론트에 내려줄 수 있도록 변환해준다.
        List<FieldError> errors = e.getConstraintViolations()
                .stream()
                .map(v -> {
                    // propertyPath 예시:
                    // "valid.name"
                    // 여기서 마지막 부분(name)만 잘라내기 위해 아래 코드 사용
                    String path = v.getPropertyPath().toString();

                    // 점(.)이 있으면 마지막 점 뒤 문자열만 사용
                    // 예: valid.name -> name
                    String field = path.contains(".")
                            ? path.substring(path.lastIndexOf('.') + 1)
                            : path;

                    return new FieldError(field, v.getMessage());
                })
                .collect(Collectors.toList());

        ErrorResponse body = ErrorResponse.of(
                "VALIDATION_ERROR",
                "요청 파라미터가 올바르지 않습니다.",
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }


    /*
     * 5. 그 외 모든 RuntimeException에 대한 안전망
     * 위에서 잡지 못한 예외가 여기로 들어온다.
     * 사용자에게는 상세 메시지를 숨기고,
     * 서버 로그로만 실제 원인을 남긴다.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException e) {

        // 내부 에러 로그 기록
        log.error("Unhandled RuntimeException", e);

        ErrorResponse body = ErrorResponse.of(
                "INTERNAL_ERROR",
                "서버 처리 중 오류가 발생했습니다."
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
