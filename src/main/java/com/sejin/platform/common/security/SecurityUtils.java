package com.sejin.platform.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/*
사용 용도:
- 컨트롤러/서비스에서 "현재 로그인한 사용자" 정보를 공통 방식으로 꺼내기 위한 유틸

필요한 이유:
- SecurityContextHolder에서 꺼내는 코드가 여기저기 반복되면 실수도 나고 코드도 지저분해짐
- 한 군데로 모아두면 읽기 쉽고, 방식이 바뀌어도 수정 포인트가 줄어듦

설명:
- 인증이 안 된 요청이면 null이 나올 수 있음
- 필요한 곳에서 null 처리(또는 예외 처리)를 팀 규칙대로 하면 됨
*/
public class SecurityUtils {

    private SecurityUtils() {
        // 유틸 클래스라서 객체 생성 못 하게 막아둠
    }

    // 현재 로그인 사용자(UserPrincipal) 반환
    public static UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal)) {
            return null;
        }

        return (UserPrincipal) principal;
    }

    // 현재 로그인 사용자 id 반환
    public static Long getCurrentUserId() {
        UserPrincipal user = getCurrentUser();
        return (user == null) ? null : user.getUserId();
    }

    // 현재 로그인 사용자 role 반환
    public static String getCurrentRole() {
        UserPrincipal user = getCurrentUser();
        return (user == null) ? null : user.getRole();
    }
}
