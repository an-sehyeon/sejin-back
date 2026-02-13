package com.sejin.platform.config.security;

import com.sejin.platform.common.security.JwtProperties;
import com.sejin.platform.common.security.JwtTokenProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/*
사용 용도:
- 스프링 시큐리티에서 어떤 요청을 허용/차단할지,
  그리고 JWT 필터를 언제 실행할지 정하는 설정

필요한 이유:
- JWT는 서버 세션을 쓰지 않는 방식이라서,
  세션 정책을 STATELESS로 바꾸고 JWT 필터를 체인에 끼워줘야 제대로 동작함
- admin/driver/plant 역할별로 접근 제한을 걸기 위해 필요함

설명:
- /api/auth/**는 로그인/재발급용이라 열어둠
- /api/admin/**, /api/driver/**, /api/plant/**는 role로 접근 제한
- 나머지는 로그인만 하면 접근 가능하도록 설정
*/
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    @Bean
    public JwtTokenProvider jwtTokenProvider(JwtProperties jwtProperties) {
        return new JwtTokenProvider(jwtProperties);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        return new JwtAuthenticationFilter(jwtTokenProvider);
    }

    /*
    사용 용도:
    - 시큐리티 필터 체인 구성

    설명:
    - JWT 필터는 UsernamePasswordAuthenticationFilter 전에 실행되게 배치함
    - 그래야 컨트롤러에 들어가기 전에 로그인 정보를 먼저 세팅할 수 있음
    */
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RestAuthenticationEntryPoint restAuthenticationEntryPoint,
            RestAccessDeniedHandler restAccessDeniedHandler
    ) throws Exception {

        http
            // JWT는 서버 세션을 안 쓰는 구조라서 보통 CSRF는 끄고 시작함
            .csrf(csrf -> csrf.disable())

            // 세션을 저장하지 않고, 요청마다 토큰으로 인증함
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 인증/권한 예외를 JSON 응답으로 통일하기 위한 처리
            .exceptionHandling(eh -> eh
                    .authenticationEntryPoint(restAuthenticationEntryPoint)
                    .accessDeniedHandler(restAccessDeniedHandler)
            )

            // React 연동 때문에 CORS 설정을 활성화
            .cors(cors -> {})

            .authorizeHttpRequests(auth -> auth
                // 로그인/토큰 재발급 같은 인증 API는 누구나 접근 가능
                .requestMatchers("/api/auth/**").permitAll()

                // 문서/테스트용(운영에서는 막아도 됨)
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                // 역할별 접근 제한
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/driver/**").hasRole("DRIVER")
                .requestMatchers("/api/plant/**").hasRole("PLANT")

                // 그 외는 로그인만 하면 접근 가능
                .anyRequest().authenticated()
            );

        // 토큰 검사 필터를 앞단에 추가해서, 컨트롤러 진입 전에 인증 정보를 세팅함
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
