package jz.pk.evcm.controller;

import jz.pk.evcm.controller.app.AuthController;
import jz.pk.evcm.dto.req.local.LoginUserDto;
import jz.pk.evcm.dto.req.local.SignupUserDto;
import jz.pk.evcm.dto.res.LoginResponse;
import jz.pk.evcm.dto.res.UserResponse;
import jz.pk.evcm.entity.User;
import jz.pk.evcm.security.JwtService;
import jz.pk.evcm.service.AuthService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private AuthService authService;
    @InjectMocks
    private AuthController controller;

    private final SignupUserDto signupDto = mock(SignupUserDto.class);
    private final LoginUserDto loginDto = mock(LoginUserDto.class);
    private final User user = mock(User.class);

    @Nested
    class RegisterUser {

        @Test
        void returnsOkWithRegisteredUser() {
            UserResponse expected = mock(UserResponse.class);
            when(authService.signupUser(signupDto)).thenReturn(expected);

            ResponseEntity<?> response = controller.registerUser(signupDto);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isSameAs(expected);
            verify(authService).signupUser(same(signupDto));
            verifyNoMoreInteractions(authService);
            verifyNoInteractions(jwtService);
        }

        @Test
        void throwsIllegalStateException_whenServiceFails() {
            when(authService.signupUser(signupDto))
                    .thenThrow(new IllegalStateException("Email already taken"));

            assertThatThrownBy(() -> controller.registerUser(signupDto))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Email already taken");
        }

    }

    @Nested
    class AuthenticateUser {

        private static final String TOKEN = "jwt-token";
        private static final long EXPIRATION = 3_600_000L;

        @Test
        void returnsOkWithTokenAndExpiration() {
            when(authService.authenticate(loginDto)).thenReturn(user);
            when(jwtService.generateToken(user)).thenReturn(TOKEN);
            when(jwtService.getExpirationTime()).thenReturn(EXPIRATION);

            ResponseEntity<?> response = controller.authenticateUser(loginDto);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody())
                    .isInstanceOf(LoginResponse.class)
                    .usingRecursiveComparison()
                    .isEqualTo(new LoginResponse(TOKEN, EXPIRATION));
        }

        @Test
        void generatesTokenForAuthenticatedUser() {
            when(authService.authenticate(loginDto)).thenReturn(user);
            when(jwtService.generateToken(user)).thenReturn(TOKEN);

            controller.authenticateUser(loginDto);

            verify(jwtService).generateToken(same(user));
        }

        @Test
        void authenticatesBeforeGeneratingToken() {
            when(authService.authenticate(loginDto)).thenReturn(user);
            when(jwtService.generateToken(user)).thenReturn(TOKEN);

            controller.authenticateUser(loginDto);

            InOrder inOrder = inOrder(authService, jwtService);
            inOrder.verify(authService).authenticate(loginDto);
            inOrder.verify(jwtService).generateToken(user);
        }

        @Test
        void throwsBadCredentialsException_andSkipsTokenGeneration_whenCredentialsAreBad() {
            when(authService.authenticate(loginDto))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> controller.authenticateUser(loginDto))
                    .isInstanceOf(BadCredentialsException.class);

            verifyNoInteractions(jwtService);
        }

        @Test
        void throwsIllegalStateException_whenJwtServiceFails() {
            when(authService.authenticate(loginDto)).thenReturn(user);
            when(jwtService.generateToken(user))
                    .thenThrow(new IllegalStateException("Signing key missing"));

            assertThatThrownBy(() -> controller.authenticateUser(loginDto))
                    .isInstanceOf(IllegalStateException.class);
        }

    }
}