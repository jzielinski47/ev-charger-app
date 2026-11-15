package jz.pk.evcm.dto.req.local;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name cannot exceed 255 characters")
        String name,

        @NotBlank(message = "Surname is required")
        @Size(max = 255, message = "Surname cannot exceed 255 characters")
        String surname
) {}
