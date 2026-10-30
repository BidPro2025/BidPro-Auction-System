package com.bidpro.service;
import com.bidpro.dao.UserDao;
import com.bidpro.entities.User;
import java.util.Arrays;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
@Service
public class CurrentUserService {
    private final UserDao users;
    public CurrentUserService(UserDao users) { this.users = users; }
    public User user() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) throw new AccessDeniedException("Please sign in");
        return users.findByEmail(auth.getName()).orElseThrow(() -> new AccessDeniedException("Account not found"));
    }
    public String id() { return user().getUserID(); }
    public boolean isAdmin() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
    public void requireApproved() { if (user().getStatus() != User.Status.APPROVED) throw new AccessDeniedException("Your account is awaiting administrator approval"); }
    public void requireRoles(User.Role... roles) {
        requireApproved();
        if (!Arrays.asList(roles).contains(user().getRole())) throw new AccessDeniedException("Your account role cannot perform this action");
    }
}
