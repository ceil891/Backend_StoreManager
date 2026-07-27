package org.example.storemanager.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret:SmartRetailProject2026_SecureKeyForJWT_MustBeAtLeast64CharactersLong_For_HS512_Security}")
    private String jwtSecret;

    // Access token hết hạn sau 1 giờ (ms)
    @Value("${jwt.access-token-expiration:3600000}")
    private long accessTokenExpiration;

    // Refresh token hết hạn sau 7 ngày (ms)
    @Value("${jwt.refresh-token-expiration:604800000}")
    private long refreshTokenExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Tạo Access Token (ngắn hạn – 1 giờ mặc định).
     */
    public String generateAccessToken(String username) {
        return buildToken(username, accessTokenExpiration, "access");
    }

    /**
     * Tạo Refresh Token (dài hạn – 7 ngày mặc định).
     */
    public String generateRefreshToken(String username) {
        return buildToken(username, refreshTokenExpiration, "refresh");
    }

    /**
     * Trả về thời gian hết hạn của refresh token (ms từ hiện tại).
     */
    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpiration;
    }

    /**
     * Trích xuất username (subject) từ JWT token.
     */
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Kiểm tra token có hợp lệ và chưa hết hạn không.
     */
    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private String buildToken(String username, long expirationMs, String tokenType) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(username)
                .claim("type", tokenType)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
