package com.bidpro.dto;

import com.bidpro.entities.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class UserDTO extends BaseDTO {
    @Size(max = 50, message = "User ID must be at most 50 characters")
    private String userID;

    @NotBlank(message = "First name is mandatory")
    @Size(max = 50, message = "First name must be at most 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is mandatory")
    @Size(max = 50, message = "Last name must be at most 50 characters")
    private String lastName;

    @NotBlank(message = "Username is mandatory")
    @Size(max = 50, message = "Username must be at most 50 characters")
    private String username;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must be at most 100 characters")
    private String email;

    @Size(max = 20, message = "Phone must be at most 20 characters")
    private String phone;

    @Size(max = 255, message = "Password hash must be at most 255 characters")
    private String passwordHash;

    @NotNull(message = "Role is mandatory")
    private User.Role role;

    @NotNull(message = "Status is mandatory")
    private User.Status status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}