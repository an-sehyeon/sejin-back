package com.sejin.platform.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	// 개발 단계에서는 로그인 화면 때문에 테스트가 계속 막힘
	// 현 단계에서는 모든 요청을 허용해서 개발을 편하게 진행한다.
	// 나중에 기능 개발이 끝나면 정책을 바꿔서 인증과 권한을 적용한다.
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
		
		// 개발 단계에서는 POST 요청 테스트를 많이 하기 때문에 CSRF 때문에 403이 뜨는 상황을 막기 위해 꺼둠
		http.csrf(csrf -> csrf.disable());
		
		//로그인 기능을 만들기 전이라 기본 로그인 폼이 뜨는 것도 꺼둠.
		http.formLogin(form -> form.disable());
		
		// 브라우저 기본 로그인 팝업도 필요 없으니 꺼둠.
		http.httpBasic(basic -> basic.disable());
		
		// 개발 단계에서는 모든 URL 요청을 허용
		http.authorizeHttpRequests(auth -> auth
					.anyRequest().permitAll()
				);
		
		// 스프링 시큐리티에 등록
		return http.build();
	}
	
}
