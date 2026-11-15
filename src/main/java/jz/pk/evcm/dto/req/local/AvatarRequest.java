package jz.pk.evcm.dto.req.local;

import jakarta.validation.constraints.NotNull;
import jz.pk.evcm.entity.UserAvatar;

/** Body of PUT /api/users/me/avatar. Values outside UserAvatar are rejected with 400. */
public record AvatarRequest(@NotNull UserAvatar avatar) {}
