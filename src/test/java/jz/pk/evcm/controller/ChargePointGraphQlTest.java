package jz.pk.evcm.controller;

import jz.pk.evcm.entity.AddressInfo;
import jz.pk.evcm.entity.ChargerPoint;
import jz.pk.evcm.entity.Connection;
import jz.pk.evcm.entity.ConnectorType;
import jz.pk.evcm.entity.User;
import jz.pk.evcm.entity.VehicleInlet;
import jz.pk.evcm.repository.ChargerPointRepository;
import jz.pk.evcm.repository.UserRepository;
import jz.pk.evcm.repository.VehicleRepository;
import jz.pk.evcm.security.JwtService;
import jz.pk.evcm.testdata.TestContainersConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.http.HttpHeaders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static jz.pk.evcm.entity.ConnectorType.*;
import static jz.pk.evcm.testdata.TestVehicleDefaults.aVehicleOwnedBy;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.graphql.http.path=/graphql/chargers",
                "spring.jpa.open-in-view=false",
                "security.jwt.secret-key=dGVzdC1vbmx5LWp3dC1zZWNyZXQta2V5LTI1Ni1iaXRzLWxvbmc=",
                "security.jwt.expiration-time=3600000",
                "opencharge.api.key=test",
                "charger.sync.enabled=false",
                "admin.email=admin@test.dev",
                "admin.password=admin"
        })
@AutoConfigureHttpGraphQlTester
@Import(TestContainersConfig.class)
class ChargePointGraphQlTest {

    private static final double KRAKOW_LAT = 50.0614;
    private static final double KRAKOW_LON = 19.9366;
    private static final double RADIUS_KM = 5.0;

    private static final String PROXIMITY_QUERY = """
            query($lat: Float, $lon: Float, $proximity: Float, $vehicleId: ID) {
              allChargerPointsInProximity(lat: $lat, lon: $lon, proximity: $proximity, vehicleId: $vehicleId) {
                id
                connections { connectorType }
              }
            }
            """;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    private final List<User> createdUsers = new ArrayList<>();
    private long chargerIdCounter = 1L;
    private long connectionIdCounter = 1L;

    @Autowired
    private HttpGraphQlTester graphQlTester;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private ChargerPointRepository chargerPointRepository;

    @AfterEach
    void cleanUp() {
        vehicleRepository.deleteAll();
        chargerPointRepository.deleteAll();
        userRepository.deleteAll(createdUsers);
    }

    private long saveChargerNearKrakow(double latOffset, ConnectorType... connectorTypes) {
        AddressInfo address = new AddressInfo();
        address.setId(chargerIdCounter);
        address.setLocation(geometryFactory.createPoint(new Coordinate(KRAKOW_LON, KRAKOW_LAT + latOffset)));

        ChargerPoint charger = new ChargerPoint();
        charger.setId(chargerIdCounter++);
        charger.setAddressInfo(address);
        charger.setConnections(Arrays.stream(connectorTypes)
                .map(type -> Connection.builder()
                        .id(connectionIdCounter++)
                        .chargerPoint(charger)
                        .connectorType(type)
                        .build())
                .toList());
        return chargerPointRepository.save(charger).getId();
    }

    private User saveUser(String email) {
        User user = userRepository.save(User.builder().email(email).name("name").surname("sur").password("1234").build());
        createdUsers.add(user);
        return user;
    }

    private long saveVehicle(User owner, VehicleInlet inlet) {
        return vehicleRepository.save(aVehicleOwnedBy(owner).supportedInlets(EnumSet.of(inlet)).build()).getId();
    }

    private GraphQlTester.Response queryProximity(HttpGraphQlTester tester, Long vehicleId) {
        return tester.document(PROXIMITY_QUERY)
                .variable("lat", KRAKOW_LAT)
                .variable("lon", KRAKOW_LON)
                .variable("proximity", RADIUS_KM)
                .variable("vehicleId", vehicleId)
                .execute();
    }

    private HttpGraphQlTester authenticatedAs(User user) {
        return graphQlTester.mutate()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtService.generateToken(user))
                .build();
    }

    private static void assertSingleError(GraphQlTester.Response response, ErrorType errorType, String message) {
        response.errors().satisfy(errors -> {
            assertThat(errors).singleElement().extracting(ResponseError::getErrorType).isEqualTo(errorType);
            assertThat(errors).singleElement().extracting(ResponseError::getMessage).isEqualTo(message);
        });
    }

    @Test
    void anonymousUserWithoutVehicleId_getsAllChargersInRadius() {
        long ccs2 = saveChargerNearKrakow(0.01, CCS2);
        long chademo = saveChargerNearKrakow(0.02, CHADEMO);

        queryProximity(graphQlTester, null)
                .path("allChargerPointsInProximity[*].id").entityList(Long.class)
                .containsExactly(ccs2, chademo);
    }

    @Test
    void ownerWithCcs2Vehicle_getsCompatibleChargers_withAllTheirConnections() {
        User owner = saveUser("owner@test.dev");
        long vehicleId = saveVehicle(owner, VehicleInlet.CCS2);
        long mixed = saveChargerNearKrakow(0.01, CCS2, CHADEMO);
        long type2 = saveChargerNearKrakow(0.02, TYPE_2);
        saveChargerNearKrakow(0.03, CHADEMO);

        GraphQlTester.Response response = queryProximity(authenticatedAs(owner), vehicleId);

        response.path("allChargerPointsInProximity[*].id").entityList(Long.class)
                .containsExactly(mixed, type2);
        response.path("allChargerPointsInProximity[0].connections[*].connectorType").entityList(String.class)
                .containsExactly("CCS2", "CHADEMO");
    }

    @Test
    void anonymousUserWithVehicleId_isRejectedAsUnauthorized() {
        long vehicleId = saveVehicle(saveUser("owner@test.dev"), VehicleInlet.CCS2);
        saveChargerNearKrakow(0.01, CCS2);

        assertSingleError(queryProximity(graphQlTester, vehicleId),
                ErrorType.UNAUTHORIZED, "Vehicle filter requires authentication");
    }

    @Test
    void invalidTokenWithoutVehicleId_getsResultsAsAnonymous() {
        long ccs2 = saveChargerNearKrakow(0.01, CCS2);
        HttpGraphQlTester invalidToken = graphQlTester.mutate()
                .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt")
                .build();

        queryProximity(invalidToken, null)
                .path("allChargerPointsInProximity[*].id").entityList(Long.class)
                .containsExactly(ccs2);
    }

    @Test
    void invalidTokenWithVehicleId_isRejectedAsUnauthorized() {
        long vehicleId = saveVehicle(saveUser("owner@test.dev"), VehicleInlet.CCS2);
        HttpGraphQlTester invalidToken = graphQlTester.mutate()
                .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt")
                .build();

        assertSingleError(queryProximity(invalidToken, vehicleId),
                ErrorType.UNAUTHORIZED, "Vehicle filter requires authentication");
    }

    @Test
    void otherUsersVehicle_isRejectedSameAsMissingVehicle() {
        long vehicleId = saveVehicle(saveUser("owner@test.dev"), VehicleInlet.CCS2);
        HttpGraphQlTester otherUser = authenticatedAs(saveUser("other@test.dev"));

        assertSingleError(queryProximity(otherUser, vehicleId), ErrorType.NOT_FOUND, "Vehicle not found");
        assertSingleError(queryProximity(otherUser, vehicleId + 1000), ErrorType.NOT_FOUND, "Vehicle not found");
    }
}
