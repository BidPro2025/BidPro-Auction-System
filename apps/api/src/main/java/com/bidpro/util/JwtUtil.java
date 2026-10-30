package com.bidpro.util;

import com.bidpro.dao.UserDao;
import com.bidpro.entities.User;
import com.bidpro.security.AuthRevokedTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Date;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
    private final Key key;
    private final long expiration;
    private final UserDao users;
    private final AuthRevokedTokenRepository revoked;
    public JwtUtil(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration:86400000}") long expiration,
            UserDao users, AuthRevokedTokenRepository revoked) {
        if (expiration < 1000 || expiration > 86400000) throw new IllegalArgumentException("JWT_EXPIRATION must be between 1000 and 86400000 milliseconds");
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); this.expiration = expiration;
        this.users = users; this.revoked = revoked;
    }
    public String generateToken(String email, String role) {
        int version = users.findByEmail(email).map(user -> user.getPasswordVersion()).orElse(0);
        return generateToken(email, role, version);
    }
    public String generateToken(String email, String role, int version) {
        return Jwts.builder().setSubject(email).setIssuer("bidpro").setAudience("bidpro-web").setId(UUID.randomUUID().toString())
            .claim("role", role).claim("passwordVersion", version).setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiration)).signWith(key).compact();
    }
    private Claims claims(String token) {
        return Jwts.parserBuilder().requireIssuer("bidpro").requireAudience("bidpro-web")
            .setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
    public String extractEmail(String token) { return claims(token).getSubject(); }
    public Instant expiresAt(String token) { return claims(token).getExpiration().toInstant(); }
    public boolean validateToken(String token) {
        if (token == null || token.length() > 8192) return false;
        Claims value;
        int version;
        try {
            value = claims(token);
            if (value.getSubject() == null || value.getId() == null || value.getIssuedAt() == null || value.getExpiration() == null) return false;
            if (!value.getExpiration().after(new Date()) || value.getIssuedAt().after(new Date(System.currentTimeMillis() + 30000))) return false;
            if (value.getExpiration().getTime() - value.getIssuedAt().getTime() > 86400000) return false;
            Number claim = value.get("passwordVersion", Number.class);
            if (claim == null) return false;
            version = claim.intValue();
        } catch (JwtException | IllegalArgumentException ex) { return false; }
        return users.findByEmail(value.getSubject())
            .map(user -> user.getStatus() != User.Status.REJECTED && user.getPasswordVersion() == version).orElse(false)
            && !revoked.existsById(tokenHash(token));
    }
    public boolean validateToken(String token, String email) { return validateToken(token) && extractEmail(token).equals(email); }
    public static String tokenHash(String token) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8))); }
        catch (java.security.NoSuchAlgorithmException exception) { throw new IllegalStateException(exception); }
    }
}
