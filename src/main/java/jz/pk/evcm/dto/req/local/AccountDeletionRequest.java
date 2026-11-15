package jz.pk.evcm.dto.req.local;

import jakarta.validation.constraints.NotBlank;

public record AccountDeletionRequest(
        @NotBlank(message = "Password is required")
        String password
) {}
