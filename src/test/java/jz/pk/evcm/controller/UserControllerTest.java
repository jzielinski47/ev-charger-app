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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("it")
@AutoConfigureWebTestClient
@Import(TestContainersConfig.class)
class UserControllerTest {

    private final List<User> createdUsers = new ArrayList<>();

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll(createdUsers);
    }

    private User saveUser(String email) {
        User user = userRepository.save(User.builder().email(email).name("Jan").surname("Kowalski").password("1234").build());
        createdUsers.add(user);
        return user;
    }

    private String bearer(User user) {
        return "Bearer " + jwtService.generateToken(user);
    }

    private WebTestClient.ResponseSpec putAvatar(User user, String body) {
        return webTestClient.put().uri("/api/users/me/avatar")
                .header(HttpHeaders.AUTHORIZATION, bearer(user))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange();
    }

    @Test
    void getCurrentUser_returnsProfileWithoutAvatarByDefault() {
        User user = saveUser("me@test.dev");

        webTestClient.get().uri("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, bearer(user))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.email").isEqualTo("me@test.dev")
                .jsonPath("$.name").isEqualTo("Jan")
                .jsonPath("$.surname").isEqualTo("Kowalski")
                .jsonPath("$.avatarSeed").doesNotExist();
    }

    @Test
    void getCurrentUser_withoutToken_isRejected() {
        webTestClient.get().uri("/api/users/me").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void updateAvatar_withSeed_savesIt() {
        User user = saveUser("me@test.dev");

        putAvatar(user, """
                {"seed": "Felix-42_x"}
                """)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.avatarSeed").isEqualTo("Felix-42_x");

        assertThat(userRepository.findByEmail("me@test.dev")).get()
                .extracting(User::getAvatarSeed).isEqualTo("Felix-42_x");
    }

    @Test
    void updateAvatar_withNullSeed_resetsToInitials() {
        User user = saveUser("me@test.dev");
        putAvatar(user, """
                {"seed": "Felix"}
                """).expectStatus().isOk();

        putAvatar(user, "{}")
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.avatarSeed").doesNotExist();

        assertThat(userRepository.findByEmail("me@test.dev")).get()
                .extracting(User::getAvatarSeed).isNull();
    }

    @Test
    void updateAvatar_withInvalidSeed_returnsBadRequest() {
        User user = saveUser("me@test.dev");

        putAvatar(user, """
                {"seed": "https://evil.example/me.png"}
                """)
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors[0].field").isEqualTo("seed");
        putAvatar(user, """
                {"seed": ""}
                """).expectStatus().isBadRequest();
        putAvatar(user, """
                {"seed": "%s"}
                """.formatted("a".repeat(65))).expectStatus().isBadRequest();

        assertThat(userRepository.findByEmail("me@test.dev")).get()
                .extracting(User::getAvatarSeed).isNull();
    }
}
