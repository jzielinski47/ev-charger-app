package jz.pk.evcm.controller;

import jz.pk.evcm.testdata.TestContainersConfig;
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
class HealthEndpointTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void health_isPublicAndReportsUpWithoutDetails() {
        webTestClient.get().uri("/actuator/health")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP")
                .jsonPath("$.components").doesNotExist();
    }

    @Test
    void otherActuatorEndpoints_areNotExposed() {
        webTestClient.get().uri("/actuator/env")
                .exchange()
                .expectStatus().is4xxClientError();
    }
}
