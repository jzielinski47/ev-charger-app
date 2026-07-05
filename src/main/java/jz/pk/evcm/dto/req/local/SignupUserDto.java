package jz.pk.evcm.dto.req.local;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupUserDto(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Surname is required")
        String surname,

        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email address")
        @Size(max = 100, message = "Email cannot exceed 100 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password
) {}