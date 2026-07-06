package jz.pk.evcm.dto.res;

import jz.pk.evcm.entity.UserRole;

import java.util.Set;

public record UserResponse(
   Long id,
   String name,
   String surname,
   String email,
   Set<UserRole> roles
) {}
