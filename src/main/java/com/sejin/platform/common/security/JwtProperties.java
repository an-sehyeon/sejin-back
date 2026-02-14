package com.sejin.platform.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/*
사용 용도:
- JWT 관련 설정값(시크릿 키, 만료시간)을 application.yml에서 읽어오기 위한 설정 클래스

필요한 이유:
- 토큰 키나 만료시간을 코드에 박아두면, 환경(local/dev/prod) 바꿀 때마다 코드 수정이 생김
- 설정 파일로 빼두면 운영 환경에서도 안전하게 관리 가능함.

설명:
- application.yml에서 sejin.jwt 아래 값을 매핑
*/
@ConfigurationProperties(prefix = "sejin.jwt")
public class JwtProperties {

    // 토큰 서명에 쓰는 키
    private String secret;

    // Access Token 만료시간(밀리초)
    // Access는 짧게 가져가고, 만료되면 Refresh로 다시 발급하는 방식이 안전
    private long accessTokenExpireMs;

    // Refresh Token 만료시간(밀리초)
    private long refreshTokenExpireMs;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getAccessTokenExpireMs() {
        return accessTokenExpireMs;
    }

    public void setAccessTokenExpireMs(long accessTokenExpireMs) {
        this.accessTokenExpireMs = accessTokenExpireMs;
    }

    public long getRefreshTokenExpireMs() {
        return refreshTokenExpireMs;
    }

    public void setRefreshTokenExpireMs(long refreshTokenExpireMs) {
        this.refreshTokenExpireMs = refreshTokenExpireMs;
    }
}
