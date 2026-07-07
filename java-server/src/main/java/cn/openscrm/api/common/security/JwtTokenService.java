package cn.openscrm.api.common.security;

import cn.openscrm.api.config.OpenScrmProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private final OpenScrmProperties properties;

    public JwtTokenService(OpenScrmProperties properties) {
        this.properties = properties;
    }

    public String issue(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(properties.getJwt().getTtlSeconds());
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiresAt))
                .signWith(signingKey())
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key signingKey() {
        return Keys.hmacShaKeyFor(properties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
