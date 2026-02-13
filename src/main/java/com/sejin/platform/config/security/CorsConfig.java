package com.sejin.platform.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.*;

import java.util.List;

/*
사용 용도:
- React(프론트)에서 백엔드 API 호출할 때 CORS에 막히지 않게 허용해주는 설정

필요한 이유:
- 프론트 주소(localhost:5173 등)와 백엔드 주소(localhost:8080)가 다르면
  브라우저가 기본적으로 API 요청을 막아버림
- 그래서 백엔드에서 "어떤 주소에서 오는 요청을 허용할지"를 명확히 지정해야 함

설명:
- allowedOrigins는 local/dev/prod 환경에 맞게 관리하는 걸 추천
- Authorization 헤더로 JWT를 보내기 때문에 관련 설정도 같이 포함함
*/
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 프론트 주소는 프로젝트 환경에 맞게 추가/수정
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:3000"
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        // 쿠키나 인증 헤더 등을 함께 쓰는 경우를 위해 true로 설정
        config.setAllowCredentials(true);

        // 프론트에서 필요한 헤더를 읽어야 할 때 사용(필요 없으면 제거해도 됨)
        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
