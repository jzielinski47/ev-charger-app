package jz.pk.evcm.controller.app;

import jakarta.validation.Valid;
import jz.pk.evcm.dto.req.local.AvatarRequest;
import jz.pk.evcm.dto.req.local.PasswordChangeRequest;
import jz.pk.evcm.dto.req.local.ProfileRequest;
import jz.pk.evcm.dto.res.LoginResponse;
import jz.pk.evcm.dto.res.UserResponse;
import jz.pk.evcm.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public UserResponse getCurrentUser(Authentication auth) {
        return userService.getCurrentUser(auth.getName());
    }

    @PutMapping
    public UserResponse updateProfile(Authentication auth, @Valid @RequestBody ProfileRequest request) {
        return userService.updateProfile(auth.getName(), request.name(), request.surname());
    }

    @PutMapping("/password")
    public LoginResponse changePassword(Authentication auth, @Valid @RequestBody PasswordChangeRequest request) {
        return userService.changePassword(auth.getName(), request.currentPassword(), request.newPassword());
    }

    @PutMapping("/avatar")
    public UserResponse updateAvatar(Authentication auth, @Valid @RequestBody AvatarRequest request) {
        return userService.updateAvatar(auth.getName(), request.seed());
    }
}
