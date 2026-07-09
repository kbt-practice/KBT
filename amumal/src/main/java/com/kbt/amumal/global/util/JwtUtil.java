package com.kbt.amumal.global.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    private final Key key; // 서명에 사용할 HMAC 키
    private final long accessTokenExpTime;   // 액세스 토큰 만료 시간 (초)
    private final long refreshTokenExpTime;  // 리프레시 토큰 만료 시간 (초)

    public JwtUtil(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration_time}") long accessTokenExpTime,
            @Value("${jwt.refresh_expiration_time}") long refreshTokenExpTime
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey); // Base64 디코딩
        this.key = Keys.hmacShaKeyFor(keyBytes); // HMAC-SHA 키 생성
        this.accessTokenExpTime = accessTokenExpTime;
        this.refreshTokenExpTime = refreshTokenExpTime;
    }

    // 액세스 토큰 생성
    public String createAccessToken(int id, String email) {
        return createToken(id, email, accessTokenExpTime);
    }

    // 리프레시 토큰 생성
    public String createRefreshToken(int id, String email) {
        return createToken(id, email, refreshTokenExpTime);
    }

    // JWT 생성
    private String createToken(int id, String email, long expireTimeMs) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime tokenValidity = now.plus(Duration.ofMillis(expireTimeMs));

        return Jwts.builder()
                .claim("id", id)
                .claim("email", email)
                .setIssuedAt(Date.from(now.toInstant()))
                .setExpiration(Date.from(tokenValidity.toInstant()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰에서 id 추출
    public int getId(String token) {
        return parseClaims(token).get("id", Integer.class);
    }

    // 토큰에서 email 추출
    public String getEmail(String token) {
        return parseClaims(token).get("email", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e); // 위변조된 토큰
            throw e;
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e); // 만료된 토큰
            throw e;
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e); // 지원하지 않는 형식
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e); // 빈 토큰
        }
        return false;
    }

    // JWT 페이로드 추출
    public Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims(); // 만료된 토큰도 claims 반환 (토큰 재발급 시 userId 필요)
        }
    }
}
