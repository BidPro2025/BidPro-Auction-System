package com.bidpro.controller;
import com.bidpro.dto.*;
import com.bidpro.service.CurrentUserService;
import com.bidpro.service.UserService;
import com.bidpro.security.AuthSessionService;
import com.bidpro.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService users; private final JwtUtil jwt; private final CurrentUserService current; private final AuthSessionService sessions;
    public AuthController(UserService users, JwtUtil jwt, CurrentUserService current, AuthSessionService sessions) { this.users = users; this.jwt = jwt; this.current = current; this.sessions = sessions; }
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Map<String,Object>>> signup(@Valid @RequestBody SignupDTO input) {
        UserDTO user = users.signup(input);
        return ResponseEntity.ok(ApiResponse.success(Map.of("user", user, "token", jwt.generateToken(user.getEmail(), user.getRole().name(), user.getSessionVersion())), "Account created. An administrator must approve it before listing or bidding."));
    }
    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<Map<String,Object>>> signin(@Valid @RequestBody SigninDTO input) {
        UserDTO user = users.signin(input);
        return ResponseEntity.ok(ApiResponse.success(Map.of("user", user, "token", jwt.generateToken(user.getEmail(), user.getRole().name(), user.getSessionVersion())), "Signed in successfully"));
    }
    @GetMapping({"/validate", "/me"})
    public ResponseEntity<ApiResponse<UserDTO>> me() { return ResponseEntity.ok(ApiResponse.success(users.toDTO(current.user()), "Session is valid")); }
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        sessions.logout(current.id(), request.getHeader("Authorization").substring(7));
        return ResponseEntity.ok(ApiResponse.success(null, "Signed out"));
    }
    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAll() {
        sessions.logoutAll(current.id());
        return ResponseEntity.ok(ApiResponse.success(null, "Signed out on all devices"));
    }
}
