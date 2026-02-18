package com.sejin.platform.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/*
사용 용도:
- JWT 토큰을 발급하고, 검증하고, 토큰 안의 값(userId/role)을 꺼내는 역할

필요한 이유:
- 토큰 관련 로직이 필터/서비스/컨트롤러에 흩어지면 수정할 때 다 깨질 가능성이 큼
- 한 군데로 모아두면 토큰 정책(만료시간, 담는 값, 키 변경 등)을 관리하기 쉬움

설명:
- Access Token: userId/role을 담아서 요청마다 사용자 확인에 사용
- Refresh Token: Access 재발급을 위한 토큰(보통 DB 저장까지 같이 관리함)
*/
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    // HMAC 서명 키(대칭키). jjwt에서 verifyWith에 넣기 좋은 타입이라 SecretKey로 고정함
    private final SecretKey signingKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        
        // 설정 누락을 빠르게 확인하기 위한 코드 추가
        if(jwtProperties.getSecret() == null || jwtProperties.getSecret().isBlank()) {
        	throw new IllegalStateException(
        			"JWT secret이 비었음. sejin.jwt.secret 설정 확인 바람"
			);
        }

        // 시크릿 문자열을 기반으로 서명 키를 생성
        // 키 길이가 너무 짧으면 예외가 날 수 있어서 운영에서는 충분히 길게 잡아야 함
        this.signingKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    // Access Token 발급
    public String createAccessToken(Long userId, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getAccessTokenExpireMs());

        return Jwts.builder()
                // subject에는 userId를 넣어서 기본 식별값으로 사용
                .setSubject(String.valueOf(userId))
                // role은 claim으로 별도 저장
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // Refresh Token 발급
    public String createRefreshToken(Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getRefreshTokenExpireMs());

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /*
    사용 용도:
    - 토큰이 정상인지 확인(위조/만료/형식 오류)

    설명:
    - true면 "형식상 정상 토큰"이라는 의미
    - 실제 사용자 상태(탈퇴/정지 등)를 더 확인할지 여부는 서비스 정책에 따라 추가하면 됨
    */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 토큰에서 userId 추출
    public Long getUserId(String token) {
        Claims claims = parseClaims(token);
        return Long.valueOf(claims.getSubject());
    }

    // 토큰에서 role 추출
    public String getRole(String token) {
        Claims claims = parseClaims(token);
        Object role = claims.get("role");
        return (role == null) ? null : String.valueOf(role);
    }

    /*
    사용 용도:
    - 토큰을 파싱해서 Claims를 꺼내는 공통 메서드

    필요한 이유:
    - 검증/추출 로직이 여기저기 퍼지면 수정이 어려워짐
    - 파싱 방식은 라이브러리 버전이 바뀌면 같이 바뀌기 때문에 한 곳에서 관리하는 게 안전함

    설명:
    - jjwt 0.12+ 부터는 parserBuilder()가 없어지고 parser()가 builder 역할을 함
    - verifyWith(signingKey)로 서명 검증 키를 넣고,
      parseSignedClaims(token)으로 서명된 토큰(일반적인 JWT)을 파싱함
    */
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)   // SecretKey 또는 byte[] 가능
                .build()
                .parseClaimsJws(token)       // 서명 검증 + Claims 파싱
                .getBody();
    }

}
