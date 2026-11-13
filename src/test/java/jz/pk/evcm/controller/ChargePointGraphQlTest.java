package jz.pk.evcm.controller;

import jz.pk.evcm.entity.AddressInfo;
import jz.pk.evcm.entity.ChargerPoint;
import jz.pk.evcm.entity.Connection;
import jz.pk.evcm.entity.ConnectorType;
import jz.pk.evcm.entity.DataProvider;
import jz.pk.evcm.entity.Operator;
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
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static jz.pk.evcm.entity.ConnectorType.*;
import static jz.pk.evcm.testdata.TestVehicleDefaults.aVehicleOwnedBy;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("it")
@AutoConfigureHttpGraphQlTester
@Import(TestContainersConfig.class)
class ChargePointGraphQlTest {

    private static final double KRAKOW_LAT = 50.0614;
    private static final double KRAKOW_LON = 19.9366;
    private static final double RADIUS_KM = 5.0;

    private static final String PROXIMITY_QUERY = """
            query($lat: Float!, $lon: Float!, $proximity: Float!, $vehicleId: ID) {
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

    private GraphQlTester.Response queryChargerById(long id) {
        return graphQlTester.document("""
                        query($id: ID!) {
                          chargerPointById(id: $id) {
                            id
                            addressInfo { id }
                            connections { connectorType }
                          }
                        }
                        """)
                .variable("id", id)
                .execute();
    }

    @Test
    void chargerPointById_returnsChargerWithAddressAndConnections() {
        long chargerId = saveChargerNearKrakow(0.01, CCS2, CHADEMO);

        GraphQlTester.Response response = queryChargerById(chargerId);

        response.errors().verify();
        response.path("chargerPointById.id").entity(Long.class).isEqualTo(chargerId);
        response.path("chargerPointById.addressInfo.id").entity(Long.class).isEqualTo(chargerId);
        response.path("chargerPointById.connections[*].connectorType").entityList(String.class)
                .containsExactly("CCS2", "CHADEMO");
    }

    @Test
    void chargerPointById_returnsOcmDetailsAndOperator() {
        long chargerId = saveChargerNearKrakow(0.01, CCS2);
        ChargerPoint charger = chargerPointRepository.findById(chargerId).orElseThrow();
        charger.setOperator(new Operator(7L, "GreenWay", "https://greenway.example", "+48 123 456 789",
                "contact@greenway.example", "faults@greenway.example"));
        charger.setDataProvider(new DataProvider(1L, "CC BY 4.0"));
        charger.setIsOperational(true);
        charger.setUsageCost("1,99 PLN/kWh");
        charger.setNumberOfPoints(2);
        charger.setDateLastVerified(Instant.parse("2024-05-10T12:30:00Z"));
        charger.setDateLastConfirmed(Instant.parse("2023-01-02T08:00:00Z"));
        chargerPointRepository.save(charger);

        GraphQlTester.Response response = graphQlTester.document("""
                        query($id: ID!) {
                          chargerPointById(id: $id) {
                            isOperational
                            usageCost
                            numberOfPoints
                            dateLastVerified
                            dateLastConfirmed
                            operator { id name websiteUrl phone email faultReportEmail }
                            dataProvider { license }
                          }
                        }
                        """)
                .variable("id", chargerId)
                .execute();

        response.errors().verify();
        response.path("chargerPointById.isOperational").entity(Boolean.class).isEqualTo(true);
        response.path("chargerPointById.usageCost").entity(String.class).isEqualTo("1,99 PLN/kWh");
        response.path("chargerPointById.numberOfPoints").entity(Integer.class).isEqualTo(2);
        response.path("chargerPointById.dateLastVerified").entity(String.class).isEqualTo("2024-05-10T12:30:00Z");
        response.path("chargerPointById.dateLastConfirmed").entity(String.class).isEqualTo("2023-01-02T08:00:00Z");
        response.path("chargerPointById.dataProvider.license").entity(String.class).isEqualTo("CC BY 4.0");
        response.path("chargerPointById.operator.name").entity(String.class).isEqualTo("GreenWay");
        response.path("chargerPointById.operator.websiteUrl").entity(String.class).isEqualTo("https://greenway.example");
        response.path("chargerPointById.operator.phone").entity(String.class).isEqualTo("+48 123 456 789");
        response.path("chargerPointById.operator.email").entity(String.class).isEqualTo("contact@greenway.example");
        response.path("chargerPointById.operator.faultReportEmail").entity(String.class)
                .isEqualTo("faults@greenway.example");
    }

    @Test
    void proximityQuery_resolvesLazyOperator() {
        long chargerId = saveChargerNearKrakow(0.01, CCS2);
        ChargerPoint charger = chargerPointRepository.findById(chargerId).orElseThrow();
        charger.setOperator(new Operator(8L, "Orlen Charge", null, null, null, null));
        charger.setDataProvider(new DataProvider(2L, "CC BY 4.0"));
        chargerPointRepository.save(charger);

        GraphQlTester.Response response = graphQlTester.document("""
                        query($lat: Float!, $lon: Float!, $proximity: Float!) {
                          allChargerPointsInProximity(lat: $lat, lon: $lon, proximity: $proximity) {
                            operator { name }
                            dataProvider { license }
                          }
                        }
                        """)
                .variable("lat", KRAKOW_LAT)
                .variable("lon", KRAKOW_LON)
                .variable("proximity", RADIUS_KM)
                .execute();

        response.errors().verify();
        response.path("allChargerPointsInProximity[0].operator.name").entity(String.class).isEqualTo("Orlen Charge");
        response.path("allChargerPointsInProximity[0].dataProvider.license").entity(String.class).isEqualTo("CC BY 4.0");
    }

    @Test
    void chargerPointById_withMissingId_returnsNotFound() {
        assertSingleError(queryChargerById(999_999L), ErrorType.NOT_FOUND, "Charger point not found");
    }

    @Test
    void proximityQueryWithoutRadius_isRejectedAsValidationError() {
        GraphQlTester.Response response = graphQlTester
                .document("{ allChargerPointsInProximity(lat: 50.06, lon: 19.93) { id } }")
                .execute();

        response.errors().satisfy(errors -> assertThat(errors).singleElement()
                .extracting(ResponseError::getErrorType)
                .isEqualTo(graphql.ErrorType.ValidationError));
        response.path("allChargerPointsInProximity").pathDoesNotExist();
    }

    @Test
    void chargerAddressFields_areResolvedWithoutErrors() {
        long chargerId = saveChargerNearKrakow(0.01, CCS2);

        GraphQlTester.Response response = graphQlTester.document("""
                        query($lat: Float!, $lon: Float!, $proximity: Float!) {
                          allChargerPointsInProximity(lat: $lat, lon: $lon, proximity: $proximity) {
                            addressInfo { id latitude longitude }
                          }
                        }
                        """)
                .variable("lat", KRAKOW_LAT)
                .variable("lon", KRAKOW_LON)
                .variable("proximity", RADIUS_KM)
                .execute();

        response.errors().verify();
        response.path("allChargerPointsInProximity[0].addressInfo.id").entity(Long.class).isEqualTo(chargerId);
        response.path("allChargerPointsInProximity[0].addressInfo.latitude").entity(Double.class)
                .isEqualTo(KRAKOW_LAT + 0.01);
        response.path("allChargerPointsInProximity[0].addressInfo.longitude").entity(Double.class)
                .isEqualTo(KRAKOW_LON);
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
                ErrorType.UNAUTHORIZED, "Unauthorized");
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
                ErrorType.UNAUTHORIZED, "Unauthorized");
    }

    @Test
    void otherUsersVehicle_isRejectedSameAsMissingVehicle() {
        long vehicleId = saveVehicle(saveUser("owner@test.dev"), VehicleInlet.CCS2);
        HttpGraphQlTester otherUser = authenticatedAs(saveUser("other@test.dev"));

        assertSingleError(queryProximity(otherUser, vehicleId), ErrorType.NOT_FOUND, "Vehicle not found");
        assertSingleError(queryProximity(otherUser, vehicleId + 1000), ErrorType.NOT_FOUND, "Vehicle not found");
    }
}
