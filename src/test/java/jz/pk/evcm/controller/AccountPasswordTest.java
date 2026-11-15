package jz.pk.evcm.controller;

import jz.pk.evcm.dto.res.LoginResponse;
import jz.pk.evcm.entity.User;
import jz.pk.evcm.repository.RefreshTokenRepository;
import jz.pk.evcm.repository.UserRepository;
import jz.pk.evcm.service.SessionService;
import jz.pk.evcm.testdata.TestContainersConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("it")
@AutoConfigureWebTestClient
@Import(TestContainersConfig.class)
class AccountPasswordTest {

    private static final String EMAIL = "password@test.dev";
    private static final String PASSWORD = "password123";

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user;

    @BeforeEach
    void createUser() {
        user = userRepository.save(User.builder()
                .email(EMAIL).name("Jan").surname("Kowalski").password(passwordEncoder.encode(PASSWORD)).build());
    }

    @AfterEach
    void cleanUp() {
        userRepository.findByEmail(EMAIL).ifPresent(userRepository::delete);
    }

    private WebTestClient.ResponseSpec putPassword(String accessToken, String currentPassword, String newPassword) {
        return webTestClient.put().uri("/api/users/me/password")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"currentPassword": "%s", "newPassword": "%s"}
                        """.formatted(currentPassword, newPassword))
                .exchange();
    }

    @Test
    void changePassword_updatesHash_revokesOtherSessions_andReturnsNewPair() {
        LoginResponse otherDevice = sessionService.startSession(user);
        LoginResponse thisDevice = sessionService.startSession(user);

        LoginResponse result = putPassword(thisDevice.token(), PASSWORD, "brandNewPass1")
                .expectStatus().isOk()
                .expectBody(LoginResponse.class)
                .returnResult().getResponseBody();

        assertThat(result).isNotNull();
        assertThat(result.refreshToken()).isNotIn(otherDevice.refreshToken(), thisDevice.refreshToken());
        assertThat(refreshTokenRepository.count()).isEqualTo(1);
        assertThat(passwordEncoder.matches("brandNewPass1",
                userRepository.findByEmail(EMAIL).orElseThrow().getPassword())).isTrue();

        webTestClient.post().uri("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"refreshToken": "%s"}
                        """.formatted(otherDevice.refreshToken()))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void changePassword_withWrongCurrentPassword_returnsFieldError() {
        LoginResponse session = sessionService.startSession(user);

        putPassword(session.token(), "wrong-password", "brandNewPass1")
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors[0].field").isEqualTo("currentPassword")
                .jsonPath("$.errors[0].message").isEqualTo("Current password is incorrect");

        assertThat(refreshTokenRepository.count()).isEqualTo(1);
        assertThat(passwordEncoder.matches(PASSWORD,
                userRepository.findByEmail(EMAIL).orElseThrow().getPassword())).isTrue();
    }

    @Test
    void changePassword_withTooShortNewPassword_returnsFieldError() {
        LoginResponse session = sessionService.startSession(user);

        putPassword(session.token(), PASSWORD, "short")
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors[0].field").isEqualTo("newPassword");
    }
}
