package jz.pk.evcm.controller;

import jz.pk.evcm.dto.res.LoginResponse;
import jz.pk.evcm.entity.User;
import jz.pk.evcm.entity.UserRole;
import jz.pk.evcm.repository.RefreshTokenRepository;
import jz.pk.evcm.repository.UserRepository;
import jz.pk.evcm.repository.VehicleRepository;
import jz.pk.evcm.security.JwtService;
import jz.pk.evcm.service.SessionService;
import jz.pk.evcm.testdata.TestContainersConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static jz.pk.evcm.testdata.TestVehicleDefaults.aVehicleOwnedBy;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("it")
@AutoConfigureWebTestClient
@Import(TestContainersConfig.class)
class AccountDeletionTest {

    private static final String PASSWORD = "password123";

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String seededAdminEmail;

    @Value("${admin.password}")
    private String seededAdminPassword;

    @AfterEach
    void cleanUp() {
        userRepository.findByEmail("delete-me@test.dev").ifPresent(user -> {
            vehicleRepository.deleteAll(vehicleRepository.findByOwnerEmail(user.getEmail()));
            userRepository.delete(user);
        });
        userRepository.findByEmail("second-admin@test.dev").ifPresent(userRepository::delete);
    }

    private User saveUser(String email, UserRole... extraRoles) {
        User user = User.builder()
                .email(email).name("Jan").surname("Kowalski").password(passwordEncoder.encode(PASSWORD)).build();
        user.assignRole(UserRole.USER);
        for (UserRole role : extraRoles) {
            user.assignRole(role);
        }
        return userRepository.save(user);
    }

    private WebTestClient.ResponseSpec deleteMe(User user, String password) {
        return webTestClient.method(HttpMethod.DELETE).uri("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtService.generateToken(user))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"password": "%s"}
                        """.formatted(password))
                .exchange();
    }

    @Test
    void deleteAccount_removesUserVehiclesAndSessions() {
        User user = saveUser("delete-me@test.dev");
        vehicleRepository.save(aVehicleOwnedBy(user).build());
        LoginResponse session = sessionService.startSession(user);

        deleteMe(user, PASSWORD).expectStatus().isNoContent();

        assertThat(userRepository.findByEmail("delete-me@test.dev")).isEmpty();
        assertThat(vehicleRepository.findByOwnerEmail("delete-me@test.dev")).isEmpty();
        assertThat(refreshTokenRepository.count()).isZero();
        webTestClient.post().uri("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"refreshToken": "%s"}
                        """.formatted(session.refreshToken()))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void deleteAccount_withWrongPassword_keepsTheAccount() {
        User user = saveUser("delete-me@test.dev");

        deleteMe(user, "wrong-password")
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors[0].field").isEqualTo("password");

        assertThat(userRepository.findByEmail("delete-me@test.dev")).isPresent();
    }

    @Test
    void deleteAccount_ofTheLastAdmin_isConflict() {
        User admin = userRepository.findByEmail(seededAdminEmail).orElseThrow();

        deleteMe(admin, seededAdminPassword)
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").isEqualTo("The last admin account cannot be deleted");

        assertThat(userRepository.findByEmail(seededAdminEmail)).isPresent();
    }

    @Test
    void deleteAccount_ofAnAdmin_isAllowedWhenAnotherAdminExists() {
        User secondAdmin = saveUser("second-admin@test.dev", UserRole.ADMIN);

        deleteMe(secondAdmin, PASSWORD).expectStatus().isNoContent();

        assertThat(userRepository.findByEmail("second-admin@test.dev")).isEmpty();
    }
}
