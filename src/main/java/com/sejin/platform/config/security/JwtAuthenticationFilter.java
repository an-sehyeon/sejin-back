package com.sejin.platform.config.security;

import com.sejin.platform.common.security.JwtTokenProvider;
import com.sejin.platform.common.security.UserPrincipal;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/*
사용 용도:
- 모든 요청에서 Authorization 헤더의 JWT를 확인하고,
  정상 토큰이면 "로그인한 사용자"로 인식시키는 필터

필요한 이유:
- JWT는 세션처럼 서버가 로그인 상태를 들고 있지 않아서,
  요청이 올 때마다 토큰을 확인하고 SecurityContext에 사용자 정보를 세팅해줘야 함

설명:
- 토큰이 정상일 때만 SecurityContext에 인증 정보를 세팅
- 토큰이 없거나 이상하면 그냥 통과시키고, 권한 체크는 시큐리티 설정에서 막히게 됨
*/
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = resolveToken(request);

        // 토큰이 있고 검증이 통과하면 로그인 상태로 만든다
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            Long userId = jwtTokenProvider.getUserId(token);
            String role = jwtTokenProvider.getRole(token);

            // hasRole("ADMIN") 같은 방식으로 체크하기 쉽도록 ROLE_ prefix를 붙여서 통일
            String authority = "ROLE_" + role;

            UserPrincipal principal = new UserPrincipal(
                    userId,
                    role,
                    List.of(new SimpleGrantedAuthority(authority))
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    // Authorization: Bearer xxx 형태에서 토큰 문자열만 뽑아낸다
    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(bearer)) {
            return null;
        }

        if (bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        return null;
    }
}
