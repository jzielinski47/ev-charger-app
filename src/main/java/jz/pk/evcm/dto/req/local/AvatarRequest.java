package jz.pk.evcm.dto.req.local;

import jakarta.validation.constraints.Pattern;

public record AvatarRequest(
        @Pattern(regexp = "^[A-Za-z0-9_-]{1,64}$",
                message = "Seed must be 1-64 characters: letters, digits, '-' or '_'")
        String seed
) {}
