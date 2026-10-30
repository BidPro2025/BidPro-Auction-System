package com.bidpro.service;

import com.bidpro.dao.PasswordResetTokenDao;
import com.bidpro.dao.UserDao;
import com.bidpro.entities.PasswordResetToken;
import com.bidpro.security.PasswordPolicy;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetService {
    private final PasswordResetTokenDao tokens;
    private final UserDao users;
    private final PasswordEncoder passwords;
    private final EmailOutboxService emails;
    private final String publicUrl;
    private final LogService audit;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, Long> requests = new HashMap<>();
    public PasswordResetService(PasswordResetTokenDao tokens, UserDao users, PasswordEncoder passwords,
            EmailOutboxService emails, @Value("${app.public-url:http://127.0.0.1:5173}") String publicUrl, LogService audit) {
        this.tokens = tokens; this.users = users; this.passwords = passwords; this.emails = emails;
        java.net.URI uri = java.net.URI.create(publicUrl);
        if (!java.util.List.of("http", "https").contains(uri.getScheme()) || uri.getHost() == null || uri.getRawQuery() != null || uri.getRawFragment() != null || uri.getRawUserInfo() != null)
            throw new IllegalArgumentException("APP_PUBLIC_URL must be an absolute HTTP(S) URL");
        this.publicUrl = publicUrl.replaceAll("/$", "");
        this.audit = audit;
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void request(String email, String client) {
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (!allow("email:" + hash(normalized), 60000) || !allow("ip:" + hash(client), 5000)) return;
        var found = users.findByEmail(normalized);
        if (found.isEmpty()) return;
        var user = users.findForUpdateByUserID(found.get().getUserID()).orElseThrow();
        tokens.invalidateForUser(user.getUserID(), LocalDateTime.now());
        byte[] bytes = new byte[32]; random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        PasswordResetToken reset = new PasswordResetToken();
        reset.setTokenHash(hash(token)); reset.setUserID(user.getUserID()); reset.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        tokens.save(reset);
        emails.enqueue(user.getEmail(), "Reset your BidPro password", "A password reset was requested for your BidPro account.\n\nOpen this link within 30 minutes:\n" +
            publicUrl + "/reset-password?token=" + token + "\n\nIf you did not request this, ignore this email.", true);
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void reset(String token, String password) {
        PasswordPolicy.validateNewPassword(password);
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) throw invalid();
        String digest = hash(token);
        String userID = tokens.findUserID(digest).orElseThrow(this::invalid);
        var user = users.findForUpdateByUserID(userID).orElseThrow(this::invalid);
        PasswordResetToken reset = tokens.findForUpdate(digest).orElseThrow(this::invalid);
        if (reset.getUsedAt() != null || !reset.getExpiresAt().isAfter(LocalDateTime.now())) throw invalid();
        user.setPasswordHash(passwords.encode(password)); user.setPasswordVersion(user.getPasswordVersion() + 1);
        user.setUpdatedAt(LocalDateTime.now()); users.save(user);
        tokens.invalidateForUser(userID, LocalDateTime.now());
        emails.enqueue(user.getEmail(), "Your BidPro password was changed", "Your password was changed successfully. Previous sessions have been signed out. If this was not you, contact your administrator immediately.", false);
        audit.logAction("USER", userID, "PASSWORD_RESET", userID, "Account recovery completed; previous sessions revoked.");
    }
    private IllegalArgumentException invalid() { return new IllegalArgumentException("This reset link is invalid or expired. Request a new link."); }
    private synchronized boolean allow(String key, long interval) {
        long now = System.currentTimeMillis();
        if (requests.size() > 10000) requests.entrySet().removeIf(entry -> now - entry.getValue() > 3600000);
        if (requests.size() > 10000) return false;
        Long previous = requests.get(key);
        if (previous != null && now - previous < interval) return false;
        requests.put(key, now); return true;
    }
    public static String hash(String text) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8))); }
        catch (java.security.NoSuchAlgorithmException exception) { throw new IllegalStateException(exception); }
    }
}
