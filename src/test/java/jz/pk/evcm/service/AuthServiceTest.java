package jz.pk.evcm.service;

import jz.pk.evcm.dto.req.local.LoginUserDto;
import jz.pk.evcm.dto.req.local.SignupUserDto;
import jz.pk.evcm.dto.res.UserResponse;
import jz.pk.evcm.entity.User;
import jz.pk.evcm.entity.UserRole;
import jz.pk.evcm.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    public void AuthService_signup_ReturnSavedUser() {
        SignupUserDto dto = new SignupUserDto(
                "Name",
                "Surname",
                "test@test.test",
                "password");

        User savedUser = new User();
        savedUser.setEmail(dto.email());
        savedUser.setName(dto.name());
        savedUser.setSurname(dto.surname());
        savedUser.setPassword("hashed");
        savedUser.assignRole(UserRole.USER);

        when(passwordEncoder.encode(dto.password())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse result = authService.signupUser(dto);

        assertNotNull(result);
        assertEquals(dto.email(), result.email());
        assertEquals(dto.name(), result.name());
        assertEquals(dto.surname(), result.surname());

        Assertions.assertThat(result.roles()).containsExactlyInAnyOrderElementsOf(List.of(UserRole.USER));

        verify(passwordEncoder, times(1)).encode(dto.password());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void AuthService_signupExistingEmail_ReturnException() {
        SignupUserDto dto = new SignupUserDto(
                "Name",
                "Surname",
                "test@test.test",
                "password");

        User existingUser = new User();
        existingUser.setEmail(dto.email());

        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(existingUser));

        ResponseStatusException exception = org.junit.jupiter.api.Assertions.assertThrows(
                ResponseStatusException.class, () -> authService.signupUser(dto)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("User with this email already exists.", exception.getReason());
        verify(userRepository, times(1)).findByEmail(dto.email());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void AuthService_authenticate_ReturnSavedUser() {
        LoginUserDto dto = new LoginUserDto("test@test.test", "password");

        User existingUser = new User();
        existingUser.setEmail(dto.email());
        existingUser.setPassword("hashed");

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(existingUser));

        User result = authService.authenticate(dto);

        assertNotNull(result);
        assertEquals(dto.email(), result.getEmail());

        verify(authManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByEmail(dto.email());
    }
}
