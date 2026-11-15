package jz.pk.evcm.controller;

import jz.pk.evcm.testdata.TestContainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("it")
@AutoConfigureWebTestClient
@Import(TestContainersConfig.class)
class ValidationErrorResponseTest {

    @Autowired
    private WebTestClient webTestClient;

    private WebTestClient.ResponseSpec postSignup(String body) {
        return webTestClient.post().uri("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange();
    }

    @Test
    void invalidFields_areListedWithTheirMessages() {
        postSignup("""
                {"name": "", "surname": "Kowalski", "email": "not-an-email", "password": "short"}
                """)
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Bad Request")
                .jsonPath("$.message").isEqualTo("Validation failed")
                .jsonPath("$.errors[*].field").value(containsInAnyOrder("name", "email", "password"))
                .jsonPath("$.errors[*].message").value(hasItem("Please provide a valid email address"));
    }

    @Test
    void wrongValueType_namesTheField() {
        webTestClient.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"email": ["a@b.c"], "password": "secret123"}
                        """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors[0].field").isEqualTo("email")
                .jsonPath("$.errors[0].message").isEqualTo("Invalid value");
    }

    @Test
    void malformedJson_returnsBadRequestWithoutFieldErrors() {
        postSignup("{not json")
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Malformed request body")
                .jsonPath("$.errors").doesNotExist();
    }
}
