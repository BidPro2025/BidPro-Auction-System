package com.bidpro.service;
import com.bidpro.custom_exceptions.AuthenticationFailureException;
import com.bidpro.custom_exceptions.InvalidInputException;
import com.bidpro.dao.UserDao;
import com.bidpro.dto.SigninDTO;
import com.bidpro.dto.SignupDTO;
import com.bidpro.dto.UserDTO;
import com.bidpro.entities.User;
import com.bidpro.security.PasswordPolicy;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@Transactional
public class UserService {
    private final UserDao users;
    private final PasswordEncoder encoder;
    private final NotificationService notifications;
    private final String dummyPasswordHash;
    private final LogService audit;
    private final CurrentUserService current;
    public UserService(UserDao users, PasswordEncoder encoder, NotificationService notifications, LogService audit, CurrentUserService current) {
        this.users = users; this.encoder = encoder; this.notifications = notifications;
        this.dummyPasswordHash = encoder.encode(UUID.randomUUID().toString());
        this.audit = audit; this.current = current;
    }
    public UserDTO signup(SignupDTO input) {
        if (input.getRole() == User.Role.ADMIN) throw new AccessDeniedException("Administrator accounts cannot be registered publicly");
        PasswordPolicy.validateNewPassword(input.getPassword());
        String email = input.getEmail().trim().toLowerCase(Locale.ROOT);
        String username = input.getUsername().trim();
        if (users.findByEmail(email).isPresent()) throw new InvalidInputException("Email already exists");
        if (users.findByUsername(username).isPresent()) throw new InvalidInputException("Username already exists");
        User user = new User(UUID.randomUUID().toString(), input.getFirstName().trim(), input.getLastName().trim(), username,
            email, input.getPhone(), encoder.encode(input.getPassword()), input.getRole(), User.Status.PENDING);
        user = users.save(user);
        notifications.notifyUser(user.getUserID(), "ACCOUNT_CREATED", "Your BidPro account was created and is awaiting administrator approval.", "/account");
        audit.logAction("USER", user.getUserID(), "ACCOUNT_CREATED", current.isAdmin() ? current.id() : user.getUserID(), "Account created in pending status.");
        return toDTO(user);
    }
    public UserDTO signin(SigninDTO input) {
        String login = input.getEmail().trim().toLowerCase(Locale.ROOT);
        User user = users.findByEmail(login).orElseGet(() -> users.findByUsername(input.getEmail().trim()).orElse(null));
        if (!PasswordPolicy.fitsBcrypt(input.getPassword())) throw new AuthenticationFailureException("Invalid email or password");
        boolean matches = encoder.matches(input.getPassword(), user == null ? dummyPasswordHash : user.getPasswordHash());
        if (user == null || !matches) throw new AuthenticationFailureException("Invalid email or password");
        if (user.getStatus() == User.Status.REJECTED) throw new AccessDeniedException("This account has been rejected. Contact an administrator.");
        audit.logAction("USER", user.getUserID(), "SIGNED_IN", user.getUserID(), "Credentials verified and session issued.");
        return toDTO(user);
    }
    public UserDTO findUserById(String id) { return toDTO(entity(id)); }
    public List<UserDTO> searchUsers(String keyword) { return users.searchUsers(keyword).stream().map(this::toDTO).toList(); }
    public List<UserDTO> findAllUsers() { return users.findAll().stream().map(this::toDTO).toList(); }
    public UserDTO updateUser(String id, UserDTO input) {
        User user = lockedEntity(id);
        if (input.getFirstName() != null && !input.getFirstName().isBlank()) user.setFirstName(input.getFirstName().trim());
        if (input.getLastName() != null && !input.getLastName().isBlank()) user.setLastName(input.getLastName().trim());
        if (input.getPhone() != null) user.setPhone(input.getPhone().trim());
        if (input.getUsername() != null && !input.getUsername().isBlank()) {
            users.findByUsername(input.getUsername().trim()).filter(other -> !other.getUserID().equals(id)).ifPresent(other -> { throw new InvalidInputException("Username already exists"); });
            user.setUsername(input.getUsername().trim());
        }
        // Email is the JWT subject. Keep it stable; never accept role, status or password hashes from a profile edit.
        user.setUpdatedAt(LocalDateTime.now());
        audit.logAction("USER", id, "PROFILE_UPDATED", id, "Profile details updated; identity and privileges preserved.");
        return toDTO(users.save(user));
    }
    public UserDTO updateStatus(String id, User.Status status) {
        if (status == null) throw new InvalidInputException("Status is required");
        User user = lockedEntity(id);
        if (user.getRole() == User.Role.ADMIN) throw new InvalidInputException("Administrator status cannot be changed here");
        if (user.getStatus() == status) return toDTO(user);
        User.Status previous = user.getStatus();
        user.setStatus(status);
        if (status == User.Status.REJECTED) user.setPasswordVersion(user.getPasswordVersion() + 1);
        user.setUpdatedAt(LocalDateTime.now()); users.save(user);
        notifications.notifyUser(id, "ACCOUNT_STATUS", "Your BidPro account status is now " + status.name().toLowerCase() + ".", "/account");
        audit.logAction("USER", id, "ACCOUNT_STATUS_CHANGED", current.isAdmin() ? current.id() : null, previous.name() + " -> " + status.name());
        return toDTO(user);
    }
    public List<User> findUsersByRole(User.Role role) { return users.findByRole(role); }
    public List<User> findUsersByStatus(User.Status status) { return users.findByStatus(status); }
    private User entity(String id) { return users.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found")); }
    private User lockedEntity(String id) { return users.findForUpdateByUserID(id).orElseThrow(() -> new EntityNotFoundException("User not found")); }
    public UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO(); dto.setUserID(user.getUserID()); dto.setFirstName(user.getFirstName()); dto.setLastName(user.getLastName());
        dto.setUsername(user.getUsername()); dto.setEmail(user.getEmail()); dto.setPhone(user.getPhone()); dto.setRole(user.getRole());
        dto.setStatus(user.getStatus()); dto.setCreatedAt(user.getCreatedAt()); dto.setUpdatedAt(user.getUpdatedAt());
        dto.setSessionVersion(user.getPasswordVersion()); return dto;
    }
}
