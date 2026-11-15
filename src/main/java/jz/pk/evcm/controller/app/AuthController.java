package jz.pk.evcm.controller.app;

import jakarta.validation.Valid;
import jz.pk.evcm.dto.req.local.LoginUserDto;
import jz.pk.evcm.dto.req.local.RefreshTokenRequest;
import jz.pk.evcm.dto.req.local.SignupUserDto;
import jz.pk.evcm.dto.res.LoginResponse;
import jz.pk.evcm.dto.res.UserResponse;
import jz.pk.evcm.entity.User;
import jz.pk.evcm.service.AuthService;
import jz.pk.evcm.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authenticationService;
    private final SessionService sessionService;

    public AuthController(AuthService authenticationService, SessionService sessionService) {
        this.authenticationService = authenticationService;
        this.sessionService = sessionService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupUserDto signupUserDto) {
        UserResponse registeredUser = authenticationService.signupUser(signupUserDto);
        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginUserDto loginUserDto) {
        User authenticatedUser = authenticationService.authenticate(loginUserDto);
        LoginResponse loginResponse = sessionService.startSession(authenticatedUser);

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return sessionService.refresh(request.refreshToken());
    }
}
