package com.sejin.platform.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.*;

import java.util.List;

/*

CORS 설정 담당

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

// 해당 클래스가 스프링 설정 클래스라는 표시(서버 뜰 때 같이 로딩됨)
@Configuration
public class CorsConfig {

	// CorsConfigurationSouce를 Bean으로 등록하면,
	// 스프링이 CORS 정책을 적용할 때 이 설정을 가져다 씀
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
    	// CORS 정책을 담는 객체(허용할 origin/method/header 등을 여기서 세팅)
        CorsConfiguration config = new CorsConfiguration();

        // 프론트 주소는 프로젝트 환경에 맞게 추가/수정
        // 허용할 프론트 주소(Origin)
        // 여기 없는 주소에서 요청이 오면 브라우저가 막히는게 정상
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:3000"
        ));

        // 허용할 HTTP 메서드
        // OPTIONS는 브라우저가 미리 물어보는요청때문에 거의 필수로 넣음
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // 허용할 요청 헤더
        // 어떤 헤드든 허용
        // Authorization(JWT)같은 헤더도 여기서 막힐 수 있어서 보통 열어둠
        config.setAllowedHeaders(List.of("*"));

        // 쿠키나 인증 헤더 등을 함께 쓰는 경우를 위해 true로 설정
        // 단, 이걸 true로 쓰면 allowedOrigins에 "*"는 못 씀(정확한 주소 지정해야 됨)
        config.setAllowCredentials(true);

        // 프론트에서 필요한 헤더를 읽어야 할 때 사용(필요 없으면 제거해도 됨)
        config.setExposedHeaders(List.of("Authorization"));

        // 어떤 URL 경로에 CORS를 적용할지 매칭하는 객체
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        
        // 전체 API 경로에 방금 만든 CORS 정책을 적용
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
