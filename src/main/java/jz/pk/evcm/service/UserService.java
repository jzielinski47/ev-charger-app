package jz.pk.evcm.service;

import jakarta.persistence.EntityNotFoundException;
import jz.pk.evcm.dto.res.LoginResponse;
import jz.pk.evcm.dto.res.UserResponse;
import jz.pk.evcm.entity.User;
import jz.pk.evcm.exception.InvalidFieldException;
import jz.pk.evcm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(EntityNotFoundException::new);
    }

    public UserResponse getCurrentUser(String email) {
        return new UserResponse(getUserByEmail(email));
    }

    @Transactional
    public UserResponse updateProfile(String email, String name, String surname) {
        User user = getUserByEmail(email);
        user.setName(name.trim());
        user.setSurname(surname.trim());
        return new UserResponse(user);
    }

    @Transactional
    public LoginResponse changePassword(String email, String currentPassword, String newPassword) {
        User user = getUserByEmail(email);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidFieldException("currentPassword", "Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        sessionService.endAllSessions(user);
        return sessionService.startSession(user);
    }

    @Transactional
    public UserResponse updateAvatar(String email, String seed) {
        User user = getUserByEmail(email);
        user.setAvatarSeed(seed);
        return new UserResponse(user);
    }
}
