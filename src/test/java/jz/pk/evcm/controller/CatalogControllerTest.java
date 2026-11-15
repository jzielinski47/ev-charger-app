package jz.pk.evcm.controller;

import jz.pk.evcm.testdata.TestContainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("it")
@AutoConfigureWebTestClient
@Import(TestContainersConfig.class)
class CatalogControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void brands_arePublicAndSeededFromTheFrontendList() {
        webTestClient.get().uri("/api/catalog/brands")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(50)
                .jsonPath("$[0].slug").isEqualTo("aiways")
                .jsonPath("$[*].slug").value(hasItems("tesla", "skoda", "mercedes-benz"));
    }

    @Test
    void models_ofABrand_includeChargingPresets() {
        webTestClient.get().uri("/api/catalog/brands/tesla/models")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].name").isEqualTo("Model 3 Long Range")
                .jsonPath("$[0].supportedInlets[0]").isEqualTo("CCS2")
                .jsonPath("$[0].batteryCapacityKwh").isEqualTo(75.0)
                .jsonPath("$[0].maxAcPowerKw").isEqualTo(11.0)
                .jsonPath("$[0].maxDcPowerKw").isEqualTo(250.0)
                .jsonPath("$[0].acPhases").isEqualTo(3);
    }

    @Test
    void models_keepEveryInletOfTheModel() {
        webTestClient.get().uri("/api/catalog/brands/nissan/models")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[?(@.name == 'Leaf 40 kWh')].supportedInlets[*]").value(containsInAnyOrder("TYPE_2", "CHADEMO"));
    }

    @Test
    void models_ofABrandWithoutPresets_isEmpty() {
        webTestClient.get().uri("/api/catalog/brands/aiways/models")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void models_ofAnUnknownBrand_isNotFound() {
        webTestClient.get().uri("/api/catalog/brands/unknown/models")
                .exchange()
                .expectStatus().isNotFound();
    }
}
