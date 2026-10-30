package com.bidpro.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminDTO extends BaseDTO {
    @Size(max = 50, message = "Admin ID must be at most 50 characters")
    private String adminId;  // Changed to adminId for consistency

    @NotBlank(message = "First name is mandatory")
    @Size(max = 50, message = "First name must be at most 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is mandatory")
    @Size(max = 50, message = "Last name must be at most 50 characters")
    private String lastName;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must be at most 100 characters")
    private String email;

    @NotNull(message = "Password is mandatory")
    @Size(
        min = 4, 
        max = 50, 
        message = "Password must be between 8 and 50 characters"
    )
    private String password;  // Changed from passwordHash to password (raw input)

    // Removed passwordHash from DTO as it should never be exposed
    // Added password confirmation field for registration
    @NotNull(message = "Password confirmation is mandatory")
    @Size(min = 4, max = 50)
    private String confirmPassword;

    // Additional security fields
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private boolean enabled = true;
}