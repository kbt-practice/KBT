package com.kbt.amumal.global.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.ZonedDateTime;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    private final Key key; // 서명에 사용할 HMAC 키
    private final long accessTokenExpTime; // 액세스 토큰 만료 시간 (초)

    public JwtUtil(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration_time}") long accessTokenExpTime
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey); // Base64 디코딩
        this.key = Keys.hmacShaKeyFor(keyBytes); // HMAC-SHA 키 생성
        this.accessTokenExpTime = accessTokenExpTime;
    }

    // 액세스 토큰 생성
    public String createAccessToken(String userId, String email) {
        return createToken(userId, email, accessTokenExpTime);
    }

    // JWT 생성
    private String createToken(String userId, String email, long expireTime) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime tokenValidity = now.plusSeconds(expireTime); // 만료 시각 계산

        return Jwts.builder()
                .claim("userId", userId) // 페이로드에 userId 저장
                .claim("email", email) // 페이로드에 email 저장
                .setIssuedAt(Date.from(now.toInstant())) // 발급 시간
                .setExpiration(Date.from(tokenValidity.toInstant())) // 만료 시간
                .signWith(key, SignatureAlgorithm.HS256) // HS256 서명
                .compact();
    }

    // 토큰에서 userId 추출
    public String getUserId(String token) {
        return parseClaims(token).get("userId", String.class);
    }

    // JWT 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e); // 위변조된 토큰
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e); // 만료된 토큰
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e); // 지원하지 않는 형식
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e); // 빈 토큰
        }
        return false;
    }

    // JWT 페이로드 추출
    public Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims(); // 만료된 토큰도 claims 반환 (토큰 재발급 시 userId 필요)
        }
    }
}
