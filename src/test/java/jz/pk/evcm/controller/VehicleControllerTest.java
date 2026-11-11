package jz.pk.evcm.controller;

import jz.pk.evcm.entity.User;
import jz.pk.evcm.entity.VehicleInlet;
import jz.pk.evcm.repository.UserRepository;
import jz.pk.evcm.repository.VehicleRepository;
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
import java.util.EnumSet;
import java.util.List;

import static jz.pk.evcm.testdata.TestVehicleDefaults.aVehicleOwnedBy;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("it")
@AutoConfigureWebTestClient
@Import(TestContainersConfig.class)
class VehicleControllerTest {

    private static final String VALID_VEHICLE = """
            {"brand": "Kia", "model": "EV6", "yearOfProduction": 2023,
             "supportedInlets": ["CCS2", "TYPE_2"], "maxDcPowerKw": 235.0, "acPhases": 3}
            """;

    private final List<User> createdUsers = new ArrayList<>();

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @AfterEach
    void cleanUp() {
        vehicleRepository.deleteAll();
        userRepository.deleteAll(createdUsers);
    }

    private User saveUser(String email) {
        User user = userRepository.save(User.builder().email(email).name("name").surname("sur").password("1234").build());
        createdUsers.add(user);
        return user;
    }

    private long saveVehicle(User owner) {
        return vehicleRepository.save(aVehicleOwnedBy(owner).supportedInlets(EnumSet.of(VehicleInlet.CCS2)).build())
                .getId();
    }

    private String bearer(User user) {
        return "Bearer " + jwtService.generateToken(user);
    }

    private WebTestClient.ResponseSpec postVehicle(User user, String body) {
        return webTestClient.post().uri("/api/vehicles")
                .header(HttpHeaders.AUTHORIZATION, bearer(user))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange();
    }

    @Test
    void addVehicle_withValidRequest_returnsVehicleWithInletsAndSpecs() {
        User owner = saveUser("owner@test.dev");

        postVehicle(owner, VALID_VEHICLE)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.supportedInlets.length()").isEqualTo(2)
                .jsonPath("$.maxDcPowerKw").isEqualTo(235.0)
                .jsonPath("$.acPhases").isEqualTo(3)
                .jsonPath("$.ownerId").isEqualTo(owner.getId().intValue());
    }

    @Test
    void addVehicle_withEmptyInlets_returnsBadRequest() {
        postVehicle(saveUser("owner@test.dev"), """
                {"brand": "Kia", "model": "EV6", "supportedInlets": []}
                """)
                .expectStatus().isBadRequest();

        assertThat(vehicleRepository.count()).isZero();
    }

    @Test
    void addVehicle_withNullInlet_returnsBadRequest() {
        postVehicle(saveUser("owner@test.dev"), """
                {"brand": "Kia", "model": "EV6", "supportedInlets": [null]}
                """)
                .expectStatus().isBadRequest();
    }

    @Test
    void getVehicleById_asOwnerWithoutAdminRole_returnsVehicle() {
        User owner = saveUser("owner@test.dev");
        long vehicleId = saveVehicle(owner);

        webTestClient.get().uri("/api/vehicles/{id}", vehicleId)
                .header(HttpHeaders.AUTHORIZATION, bearer(owner))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo((int) vehicleId)
                .jsonPath("$.supportedInlets[0]").isEqualTo("CCS2");
    }

    @Test
    void getVehicleById_ofOtherUser_returnsNotFound() {
        long vehicleId = saveVehicle(saveUser("owner@test.dev"));
        User other = saveUser("other@test.dev");

        webTestClient.get().uri("/api/vehicles/{id}", vehicleId)
                .header(HttpHeaders.AUTHORIZATION, bearer(other))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getVehicles_withoutToken_isRejected() {
        webTestClient.get().uri("/api/vehicles")
                .exchange()
                .expectStatus().isForbidden();
    }
}
