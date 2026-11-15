package jz.pk.evcm.controller;

import jz.pk.evcm.dto.res.LoginResponse;
import jz.pk.evcm.entity.User;
import jz.pk.evcm.repository.RefreshTokenRepository;
import jz.pk.evcm.repository.UserRepository;
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
class AuthSessionFlowTest {

    private static final String EMAIL = "session@test.dev";
    private static final String PASSWORD = "password123";

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

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
        userRepository.delete(user);
    }

    private LoginResponse login() {
        return webTestClient.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"email": "%s", "password": "%s"}
                        """.formatted(EMAIL, PASSWORD))
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginResponse.class)
                .returnResult().getResponseBody();
    }

    private WebTestClient.ResponseSpec post(String path, String refreshToken) {
        return webTestClient.post().uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"refreshToken": "%s"}
                        """.formatted(refreshToken))
                .exchange();
    }

    @Test
    void login_returnsAccessAndRefreshTokens() {
        LoginResponse session = login();

        assertThat(session.token()).isNotBlank();
        assertThat(session.refreshToken()).isNotBlank();
        assertThat(session.refreshExpiresIn()).isPositive();
        assertThat(refreshTokenRepository.count()).isEqualTo(1);
    }

    @Test
    void refresh_rotatesTheToken() {
        LoginResponse first = login();

        LoginResponse second = post("/api/auth/refresh", first.refreshToken())
                .expectStatus().isOk()
                .expectBody(LoginResponse.class)
                .returnResult().getResponseBody();

        assertThat(second).isNotNull();
        assertThat(second.refreshToken()).isNotEqualTo(first.refreshToken());
        webTestClient.get().uri("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + second.token())
                .exchange()
                .expectStatus().isOk();

        post("/api/auth/refresh", first.refreshToken())
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid or expired refresh token");
    }

    @Test
    void refresh_withUnknownToken_isUnauthorized() {
        post("/api/auth/refresh", "unknown").expectStatus().isUnauthorized();
    }

    @Test
    void refresh_withoutToken_isBadRequest() {
        webTestClient.post().uri("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors[0].field").isEqualTo("refreshToken");
    }
}
