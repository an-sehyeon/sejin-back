package com.sejin.platform.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

// 성공 응답을 프로젝트 전체에서 한 형태로 통일시키기 위한 클래스
// 프론트는 항상 success/code/message/data만 보면 되게 한다.

@JsonInclude(JsonInclude.Include.NON_NULL)	// data가 null이면 응답에서 data를 숨김
public class ApiResponse<T> {
	
    private final boolean success;
    private final String code;
    private final String message;
    private final T data;
	
	private ApiResponse(boolean success, String code, String message, T data) {
		this.success = success;
		this.code = code;
		this.message = message;
		this.data = data;
	}
	
	// 데이터가 있는 성공 응답
	public static <T> ApiResponse<T> ok(T data){
		return new ApiResponse<>(true, "SUCCESS","요청 성공",data);
	}
	
	// 데이터가 없는 성공 응답(저장/삭제 같은 경우)
	public static ApiResponse<Void> ok(){
		return new ApiResponse<>(true, "SUCCESS", "요청 성공", null);
	}
	
	public boolean isSuccess() {return success;}
	public String getCode() {return code;}
	public String getmessage() {return message;}
	public T getData() {return data;}

}
