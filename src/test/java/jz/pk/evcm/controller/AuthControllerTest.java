package jz.pk.evcm.controller;

import jz.pk.evcm.controller.app.AuthController;
import jz.pk.evcm.dto.req.local.LoginUserDto;
import jz.pk.evcm.dto.req.local.RefreshTokenRequest;
import jz.pk.evcm.dto.req.local.SignupUserDto;
import jz.pk.evcm.dto.res.LoginResponse;
import jz.pk.evcm.dto.res.UserResponse;
import jz.pk.evcm.entity.User;
import jz.pk.evcm.exception.InvalidRefreshTokenException;
import jz.pk.evcm.service.AuthService;
import jz.pk.evcm.service.SessionService;
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
    private SessionService sessionService;
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
            verifyNoInteractions(sessionService);
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

        private final LoginResponse session = new LoginResponse("jwt-token", 3_600_000L, "refresh-token", 2_592_000_000L);

        @Test
        void returnsOkWithSessionTokens() {
            when(authService.authenticate(loginDto)).thenReturn(user);
            when(sessionService.startSession(user)).thenReturn(session);

            ResponseEntity<?> response = controller.authenticateUser(loginDto);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isSameAs(session);
        }

        @Test
        void authenticatesBeforeStartingSession() {
            when(authService.authenticate(loginDto)).thenReturn(user);
            when(sessionService.startSession(user)).thenReturn(session);

            controller.authenticateUser(loginDto);

            InOrder inOrder = inOrder(authService, sessionService);
            inOrder.verify(authService).authenticate(loginDto);
            inOrder.verify(sessionService).startSession(same(user));
        }

        @Test
        void throwsBadCredentialsException_andSkipsSession_whenCredentialsAreBad() {
            when(authService.authenticate(loginDto))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> controller.authenticateUser(loginDto))
                    .isInstanceOf(BadCredentialsException.class);

            verifyNoInteractions(sessionService);
        }
    }

    @Nested
    class Refresh {

        @Test
        void returnsNewSessionFromService() {
            LoginResponse session = new LoginResponse("new-jwt", 1L, "new-refresh", 2L);
            when(sessionService.refresh("old-refresh")).thenReturn(session);

            assertThat(controller.refresh(new RefreshTokenRequest("old-refresh"))).isSameAs(session);
        }

        @Test
        void propagatesInvalidRefreshToken() {
            when(sessionService.refresh("bad")).thenThrow(new InvalidRefreshTokenException());

            assertThatThrownBy(() -> controller.refresh(new RefreshTokenRequest("bad")))
                    .isInstanceOf(InvalidRefreshTokenException.class);
        }
    }

    @Nested
    class Logout {

        @Test
        void endsSessionAndReturnsNoContent() {
            ResponseEntity<Void> response = controller.logout(new RefreshTokenRequest("refresh"));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(sessionService).endSession("refresh");
        }
    }
}
