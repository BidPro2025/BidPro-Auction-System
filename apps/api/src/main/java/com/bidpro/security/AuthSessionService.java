package com.bidpro.security;

import com.bidpro.dao.UserDao;
import com.bidpro.util.JwtUtil;
import com.bidpro.service.LogService;
import java.time.Instant;
import java.time.LocalDateTime;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthSessionService {
    private final UserDao users;
    private final AuthRevokedTokenRepository revoked;
    private final JwtUtil jwt;
    private final LogService audit;

    public AuthSessionService(UserDao users, AuthRevokedTokenRepository revoked, JwtUtil jwt, LogService audit) {
        this.users = users; this.revoked = revoked; this.jwt = jwt; this.audit = audit;
    }

    public void logout(String userID, String token) {
        // Serialize concurrent logout requests for one owner instead of racing unique inserts.
        users.findForUpdateByUserID(userID).orElseThrow(() -> new AccessDeniedException("Account not found"));
        String hash = JwtUtil.tokenHash(token);
        revoked.deleteByExpiresAtBefore(Instant.now());
        if (!revoked.existsById(hash)) revoked.saveAndFlush(new AuthRevokedToken(hash, jwt.expiresAt(token)));
        audit.logAction("USER", userID, "SIGNED_OUT", userID, "Current session revoked.");
    }

    public void logoutAll(String userID) {
        var user = users.findForUpdateByUserID(userID).orElseThrow(() -> new AccessDeniedException("Account not found"));
        user.setPasswordVersion(user.getPasswordVersion() + 1);
        user.setUpdatedAt(LocalDateTime.now());
        users.saveAndFlush(user);
        revoked.deleteByExpiresAtBefore(Instant.now());
        audit.logAction("USER", userID, "ALL_SESSIONS_REVOKED", userID, "Every existing session was revoked.");
    }
}
