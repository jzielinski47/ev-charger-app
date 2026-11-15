package jz.pk.evcm.controller;

import jz.pk.evcm.entity.User;
import jz.pk.evcm.repository.UserRepository;
import jz.pk.evcm.security.JwtService;
import jz.pk.evcm.security.RateLimitFilter;
import jz.pk.evcm.testdata.TestContainersConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("it")
@AutoConfigureWebTestClient
@Import(TestContainersConfig.class)
@TestPropertySource(properties = {
        "rate-limit.enabled=true",
        "rate-limit.anonymous-capacity=1",
        "rate-limit.authenticated-capacity=3"
})
class RateLimitIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Value("${admin.email}")
    private String adminEmail;

    private User user;

    @AfterEach
    void cleanUp() {
        if (user != null) {
            userRepository.delete(user);
        }
    }

    private WebTestClient.ResponseSpec getVehicles(String bearer) {
        WebTestClient.RequestHeadersSpec<?> request = webTestClient.get().uri("/api/vehicles");
        if (bearer != null) {
            request = request.header(HttpHeaders.AUTHORIZATION, bearer);
        }
        return request.exchange();
    }

    @Test
    void limitsAnonymousAndAuthenticatedRequests_butNotAdmins() {
        getVehicles(null).expectStatus().isUnauthorized();
        getVehicles(null)
                .expectStatus().isEqualTo(429)
                .expectHeader().exists(HttpHeaders.RETRY_AFTER)
                .expectBody()
                .jsonPath("$.error").isEqualTo("Too Many Requests");

        user = userRepository.save(User.builder()
                .email("limited@test.dev").name("name").surname("sur").password("1234").build());
        String userBearer = "Bearer " + jwtService.generateToken(user);

        for (int remaining = 2; remaining >= 0; remaining--) {
            getVehicles(userBearer)
                    .expectStatus().isOk()
                    .expectHeader().valueEquals(RateLimitFilter.REMAINING_HEADER, String.valueOf(remaining));
        }
        getVehicles(userBearer).expectStatus().isEqualTo(429);

        User admin = userRepository.findByEmail(adminEmail).orElseThrow();
        String adminBearer = "Bearer " + jwtService.generateToken(admin);
        for (int i = 0; i < 10; i++) {
            getVehicles(adminBearer).expectStatus().isOk();
        }
    }
}
