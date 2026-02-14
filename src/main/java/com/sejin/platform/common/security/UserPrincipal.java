package com.sejin.platform.common.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/*
사용 용도:
- 스프링 시큐리티에서 "현재 로그인한 사용자" 정보를 들고 다니기 위한 객체

필요한 이유:
- JWT는 요청마다 토큰으로 사용자를 확인해야 해서, 토큰에서 꺼낸 userId/role 같은 값을
  SecurityContext에 넣어둘 객체가 필요함
- 컨트롤러/서비스에서 "누가 요청했는지"를 공통 방식으로 꺼내 쓰기 위해 필요함

설명:
- 최소로 userId, role, authorities 정도만 들고 가는 형태로 구성함
- username/password는 JWT 인증에서는 필수는 아니라서, 필요한 수준으로만 구현함
*/
public class UserPrincipal implements UserDetails {

    // 로그인 사용자 식별값(예: memberId 같은 역할)
    private final Long userId;

    // 역할 구분(ADMIN/DRIVER/PLANT)
    private final String role;

    // 스프링 시큐리티 권한 목록(ROLE_ADMIN 같은 형태)
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long userId, String role, Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.role = role;
        this.authorities = authorities;
    }

    // 현재 로그인 사용자 id 반환
    public Long getUserId() {
        return userId;
    }

    // 현재 로그인 사용자 role 반환
    public String getRole() {
        return role;
    }

    // 시큐리티 권한 목록 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /*
    사용 용도:
    - UserDetails 규격 때문에 구현은 필요함

    설명:
    - 프로젝트에서 username을 별도로 쓰지 않는다면 userId 문자열로 반환해도 문제 없음
    */
    @Override
    public String getUsername() {
        return String.valueOf(userId);
    }

    /*
    사용 용도:
    - UserDetails 규격 때문에 구현은 필요함

    설명:
    - JWT 인증에서는 비밀번호를 SecurityContext에 들고 있을 필요가 없어서 빈 문자열로 처리
    - 나중에 폼 로그인/비밀번호 인증을 붙이면 그때 구조를 확장하면 됨
    */
    @Override
    public String getPassword() {
        return "";
    }

    // 계정 만료 정책을 따로 안 쓰면 true 고정
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정 잠금 정책을 따로 안 쓰면 true 고정
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 인증 정보(비밀번호) 만료 정책을 따로 안 쓰면 true 고정
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 활성/비활성 정책을 따로 안 쓰면 true 고정
    @Override
    public boolean isEnabled() {
        return true;
    }
}
