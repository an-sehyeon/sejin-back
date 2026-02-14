package com.sejin.platform.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/*
사용 용도:
- 로그인 안 된 상태에서 인증이 필요한 API를 호출하면 401을 JSON으로 내려주는 역할

필요한 이유:
- 기본 시큐리티 동작은 화면 리다이렉트 같은 형태가 섞일 수 있음
- 우리 프로젝트는 API 서버라서 응답을 JSON으로 통일하는 게 필요함

설명:
- 여기 응답 포맷은 나중에 common/error의 ErrorResponse 규격으로 맞춰서 통일하는 걸 추천
*/
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = Map.of(
                "code", "AUTH_401",
                "message", "로그인이 필요함",
                "path", request.getRequestURI()
        );

        objectMapper.writeValue(response.getWriter(), body);
    }
}
