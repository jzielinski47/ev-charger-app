package jz.pk.evcm.service;

import jakarta.persistence.EntityNotFoundException;
import jz.pk.evcm.dto.req.local.LoginUserDto;
import jz.pk.evcm.dto.req.local.SignupUserDto;
import jz.pk.evcm.dto.res.UserResponse;
import jz.pk.evcm.entity.User;
import jz.pk.evcm.entity.UserRole;
import jz.pk.evcm.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    public UserResponse signupUser(SignupUserDto inputDto) {

        if (userRepository.findByEmail(inputDto.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User with this email already exists.");
        }

        User user = new User();
        user.setEmail(inputDto.email());
        user.setPassword(passwordEncoder.encode(inputDto.password()));

        user.setName(inputDto.name());
        user.setSurname(inputDto.surname());
        user.assignRole(UserRole.USER);

        User savedUser = userRepository.save(user);
        return new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getSurname(),
                savedUser.getEmail(),
                savedUser.getRoles()
        );
    }

    public User authenticate(LoginUserDto inputDto) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        inputDto.email(),
                        inputDto.password()
                )
        );

       return userRepository.findByEmail(inputDto.email())
                .orElseThrow(EntityNotFoundException::new);
    }
}
