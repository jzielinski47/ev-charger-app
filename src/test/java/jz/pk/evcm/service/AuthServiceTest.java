package jz.pk.evcm.service;

import jz.pk.evcm.dto.req.local.LoginUserDto;
import jz.pk.evcm.dto.req.local.SignupUserDto;
import jz.pk.evcm.entity.User;
import jz.pk.evcm.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authManager;

    @InjectMocks
    private AuthService authService;

    @Test
    public void testSignupUser_Success() {
        SignupUserDto dto = new SignupUserDto(
                "Test",
                "User",
                "test@test.test",
                "password");

        User savedUser = new User();
        savedUser.setEmail(dto.email());
        savedUser.setName(dto.name());
        savedUser.setSurname(dto.surname());
        savedUser.setPassword("hashed");

        when(passwordEncoder.encode(dto.password())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = authService.signupUser(dto);
        assertNotNull(result);
        assertEquals("test@test.test", result.getEmail());
        assertEquals("hashed", result.getPassword());

        verify(passwordEncoder, times(1)).encode(dto.password());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testAuthenticate_Success() {
        LoginUserDto dto = new LoginUserDto("test@test.test", "password");

        User existingUser = new User();
        existingUser.setEmail(dto.email());
        existingUser.setPassword("hashed");

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(existingUser));

        User result = authService.authenticate(dto);

        assertNotNull(result);
        assertEquals("test@test.test", result.getEmail());

        verify(authManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByEmail(dto.email());
    }
}
