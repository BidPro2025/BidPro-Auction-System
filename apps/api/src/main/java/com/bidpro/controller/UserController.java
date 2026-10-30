package com.bidpro.controller;
import com.bidpro.dto.*;
import com.bidpro.service.CurrentUserService;
import com.bidpro.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService users; private final CurrentUserService current;
    public UserController(UserService users, CurrentUserService current) { this.users = users; this.current = current; }
    @GetMapping("/me") public ResponseEntity<ApiResponse<UserDTO>> me() { return ResponseEntity.ok(ApiResponse.success(users.toDTO(current.user()), "Profile retrieved")); }
    @PutMapping("/me") public ResponseEntity<ApiResponse<UserDTO>> update(@Valid @RequestBody ProfileRequest input) {
        UserDTO dto = new UserDTO(); dto.setFirstName(input.firstName()); dto.setLastName(input.lastName()); dto.setUsername(input.username()); dto.setPhone(input.phone());
        return ResponseEntity.ok(ApiResponse.success(users.updateUser(current.id(), dto), "Profile updated"));
    }
    public record ProfileRequest(@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max=50) String firstName,
        @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max=50) String lastName,
        @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(min=3,max=50)
        @jakarta.validation.constraints.Pattern(regexp="[A-Za-z0-9_.-]+",message="Username may contain only letters, numbers, dots, underscores and hyphens") String username,
        @jakarta.validation.constraints.Size(max=20) String phone) {}
}
