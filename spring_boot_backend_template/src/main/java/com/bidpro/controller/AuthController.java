
package com.bidpro.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bidpro.dto.ApiResponse;
import com.bidpro.dto.SigninDTO;
import com.bidpro.dto.SignupDTO;
import com.bidpro.dto.UserDTO;
import com.bidpro.service.UserService;
import com.bidpro.util.JwtUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Map<String, Object>>> signup(@Valid @RequestBody SignupDTO signupDTO) {
        try {
            UserDTO user = userService.signup(signupDTO);
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

            Map<String, Object> response = new HashMap<>();
            response.put("user", user);
            response.put("token", token);

            return ResponseEntity.ok(ApiResponse.success(response, "Signup successful"));
        } catch (Exception e) {
            return new ResponseEntity<>(ApiResponse.error("Failed to signup: " + e.getMessage(), "INTERNAL_ERROR"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<Map<String, Object>>> signin(@Valid @RequestBody SigninDTO signinDTO) {
        try {
            UserDTO user = userService.signin(signinDTO);
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

            Map<String, Object> response = new HashMap<>();
            response.put("user", user);
            response.put("token", token);

            return ResponseEntity.ok(ApiResponse.success(response, "Signin successful"));
        } catch (Exception e) {
            return new ResponseEntity<>(ApiResponse.error("Failed to signin: " + e.getMessage(), "INVALID_CREDENTIALS"), HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<?>> validateToken(@RequestHeader("Authorization") String header) {
        try {
            if (header == null || !header.startsWith("Bearer ")) {
                return new ResponseEntity<>(ApiResponse.error("Missing or invalid token", "INVALID_TOKEN"), HttpStatus.BAD_REQUEST);
            }
            String token = header.substring(7);
            String email = jwtUtil.extractEmail(token); // Changed from getEmailFromToken to extractEmail
            if (jwtUtil.validateToken(token, email)) {
                return ResponseEntity.ok(ApiResponse.success(null, "Token is valid"));
            } else {
                return new ResponseEntity<>(ApiResponse.error("Token invalid or expired", "INVALID_TOKEN"), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(ApiResponse.error("Failed to validate token: " + e.getMessage(), "INTERNAL_ERROR"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
