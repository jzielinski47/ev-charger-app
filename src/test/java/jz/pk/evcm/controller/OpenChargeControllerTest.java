package jz.pk.evcm.controller;

import com.sun.net.httpserver.HttpServer;
import jz.pk.evcm.repository.ChargerPointRepository;
import jz.pk.evcm.repository.UserRepository;
import jz.pk.evcm.security.JwtService;
import jz.pk.evcm.testdata.TestContainersConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("it")
@AutoConfigureWebTestClient
@Import(TestContainersConfig.class)
class OpenChargeControllerTest {

    private static final String OCM_RESPONSE = """
            [
              {"id": 201, "addressInfo": {"id": 201, "latitude": 50.06, "longitude": 19.93}, "connections": []},
              {"id": 202, "addressInfo": {"id": 202, "latitude": 50.07, "longitude": 19.94}, "connections": []}
            ]
            """;

    private static final AtomicReference<String> LAST_OCM_QUERY = new AtomicReference<>();
    private static final HttpServer OCM = startFakeOcm();

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChargerPointRepository chargerPointRepository;

    @Value("${admin.email}")
    private String adminEmail;

    @DynamicPropertySource
    static void ocmBaseUrl(DynamicPropertyRegistry registry) {
        registry.add("opencharge.api.base-url", () -> "http://localhost:" + OCM.getAddress().getPort());
    }

    private static HttpServer startFakeOcm() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.createContext("/poi", exchange -> {
                LAST_OCM_QUERY.set(exchange.getRequestURI().getQuery());
                byte[] body = OCM_RESPONSE.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length);
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(body);
                }
            });
            server.start();
            return server;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @AfterAll
    static void stopFakeOcm() {
        OCM.stop(0);
    }

    @AfterEach
    void cleanUp() {
        chargerPointRepository.deleteAll();
    }

    private String adminBearer() {
        return "Bearer " + jwtService.generateToken(userRepository.findByEmail(adminEmail).orElseThrow());
    }

    @Test
    void preview_forwardsLocationAndMaxResultsToOcm_withoutSaving() {
        webTestClient.get().uri("/api/admin/chargers?latitude=52.23&longitude=21.01&distance=7.5&maxResults=250")
                .header(HttpHeaders.AUTHORIZATION, adminBearer())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2);

        assertThat(LAST_OCM_QUERY.get())
                .contains("latitude=52.23", "longitude=21.01", "distance=7.5", "maxresults=250");
        assertThat(chargerPointRepository.count()).isZero();
    }

    @Test
    void preview_withoutLocation_isBadRequest() {
        webTestClient.get().uri("/api/admin/chargers")
                .header(HttpHeaders.AUTHORIZATION, adminBearer())
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void preview_withTooManyResults_isBadRequest() {
        webTestClient.get().uri("/api/admin/chargers?latitude=52.23&longitude=21.01&distance=7.5&maxResults=5001")
                .header(HttpHeaders.AUTHORIZATION, adminBearer())
                .exchange()
                .expectStatus().isBadRequest();
    }
}
