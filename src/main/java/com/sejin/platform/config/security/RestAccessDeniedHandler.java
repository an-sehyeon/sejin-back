package com.sejin.platform.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/*
사용 용도:
- 로그인은 했는데 권한(role)이 부족해서 막힐 때 403을 JSON으로 내려주는 역할

필요한 이유:
- admin/driver/plant 역할이 나뉘는 구조에서는 권한 막힘 케이스가 자주 발생함
- 응답 포맷을 통일해두면 프론트에서 처리하기 쉬움

설명:
- 여기 응답 포맷은 나중에 common/error의 ErrorResponse 규격으로 맞춰서 통일하는 걸 추천
*/
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = Map.of(
                "code", "AUTH_403",
                "message", "접근 권한이 없음",
                "path", request.getRequestURI()
        );

        objectMapper.writeValue(response.getWriter(), body);
    }
}
