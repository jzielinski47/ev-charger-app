package jz.pk.evcm.service;

import jakarta.persistence.EntityNotFoundException;
import jz.pk.evcm.dto.res.UserResponse;
import jz.pk.evcm.entity.User;
import jz.pk.evcm.entity.UserAvatar;
import jz.pk.evcm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(EntityNotFoundException::new);
    }

    public UserResponse getCurrentUser(String email) {
        return new UserResponse(getUserByEmail(email));
    }

    @Transactional
    public UserResponse updateAvatar(String email, UserAvatar avatar) {
        User user = getUserByEmail(email);
        user.setAvatar(avatar);
        return new UserResponse(user);
    }
}
