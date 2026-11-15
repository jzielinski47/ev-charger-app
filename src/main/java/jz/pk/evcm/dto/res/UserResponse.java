package jz.pk.evcm.dto.res;

import jz.pk.evcm.entity.User;
import jz.pk.evcm.entity.UserRole;

import java.util.Set;

public record UserResponse(
   Long id,
   String name,
   String surname,
   String email,
   Set<UserRole> roles,
   String avatarSeed
) {
    public UserResponse(User user) {
        this(user.getId(), user.getName(), user.getSurname(), user.getEmail(), Set.copyOf(user.getRoles()), user.getAvatarSeed());
    }
}
