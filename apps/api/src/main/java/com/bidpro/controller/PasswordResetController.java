package com.bidpro.controller;

import com.bidpro.dto.ApiResponse;
import com.bidpro.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/auth")
public class PasswordResetController {
    private final PasswordResetService recovery;
    public PasswordResetController(PasswordResetService recovery) { this.recovery = recovery; }
    @PostMapping("/forgot-password")
    public ApiResponse<Void> request(@Valid @RequestBody ForgotRequest input, HttpServletRequest request) {
        recovery.request(input.email(), request.getRemoteAddr());
        return ApiResponse.success(null, "If an account uses this email, password reset instructions have been sent.");
    }
    @PostMapping("/reset-password")
    public ApiResponse<Void> reset(@Valid @RequestBody ResetRequest input) {
        recovery.reset(input.token(), input.password());
        return ApiResponse.success(null, "Password changed. Sign in with your new password.");
    }
    public record ForgotRequest(@NotBlank @Email @Size(max=100) String email) {}
    public record ResetRequest(@NotBlank @Size(max=128) String token, @NotBlank @Size(min=8,max=72) String password) {}
}
