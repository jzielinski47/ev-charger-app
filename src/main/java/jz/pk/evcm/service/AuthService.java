package jz.pk.evcm.service;

import jz.pk.evcm.dto.req.local.LoginUserDto;
import jz.pk.evcm.dto.req.local.SignupUserDto;
import jz.pk.evcm.entity.User;
import jz.pk.evcm.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
    }

    public User signupUser(SignupUserDto inputDto) {
        User user = new User();
        user.setEmail(inputDto.email());
        user.setPassword(passwordEncoder.encode(inputDto.password()));

        user.setName(inputDto.name());
        user.setSurname(inputDto.surname());

        return userRepository.save(user);
    }

    public User authenticate(LoginUserDto inputDto) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        inputDto.email(),
                        inputDto.password()
                )
        );

        return userRepository.findByEmail(inputDto.email())
                .orElseThrow();
    }
}
