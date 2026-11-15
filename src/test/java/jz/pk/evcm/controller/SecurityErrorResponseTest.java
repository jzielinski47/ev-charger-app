package jz.pk.evcm.controller;

import jz.pk.evcm.entity.User;
import jz.pk.evcm.repository.UserRepository;
import jz.pk.evcm.security.JwtService;
import jz.pk.evcm.testdata.TestContainersConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("it")
@AutoConfigureWebTestClient
@Import(TestContainersConfig.class)
class SecurityErrorResponseTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @AfterEach
    void cleanUp() {
        if (user != null) {
            userRepository.delete(user);
        }
    }

    @Test
    void missingToken_returnsUnauthorizedJson() {
        webTestClient.get().uri("/api/users/me")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Unauthorized")
                .jsonPath("$.message").isEqualTo("Authentication required");
    }

    @Test
    void invalidToken_returnsUnauthorizedJson() {
        webTestClient.get().uri("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Unauthorized");
    }

    @Test
    void nonAdminOnAdminEndpoint_returnsForbiddenJson() {
        user = userRepository.save(User.builder()
                .email("plain@test.dev").name("Jan").surname("Kowalski").password("1234").build());

        webTestClient.get().uri("/api/admin/chargers")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtService.generateToken(user))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Forbidden")
                .jsonPath("$.message").isEqualTo("Access denied");
    }
}
