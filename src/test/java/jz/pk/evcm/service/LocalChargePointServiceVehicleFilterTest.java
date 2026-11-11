package jz.pk.evcm.service;

import jakarta.persistence.EntityNotFoundException;
import jz.pk.evcm.dto.res.ChargerPointResponse;
import jz.pk.evcm.entity.AddressInfo;
import jz.pk.evcm.entity.ChargerPoint;
import jz.pk.evcm.entity.Connection;
import jz.pk.evcm.entity.ConnectorType;
import jz.pk.evcm.entity.CurrentType;
import jz.pk.evcm.entity.User;
import jz.pk.evcm.entity.Vehicle;
import jz.pk.evcm.entity.VehicleInlet;
import jz.pk.evcm.testdata.TestContainersConfig;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static jz.pk.evcm.entity.ConnectorType.*;
import static jz.pk.evcm.testdata.TestVehicleDefaults.aVehicleOwnedBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestContainersConfig.class, LocalChargePointService.class})
class LocalChargePointServiceVehicleFilterTest {

    private static final double KRAKOW_LAT = 50.0614;
    private static final double KRAKOW_LON = 19.9366;
    private static final double RADIUS_KM = 5.0;
    private static final String OWNER_EMAIL = "owner@test.dev";
    private static final String OTHER_EMAIL = "other@test.dev";

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Autowired
    private LocalChargePointService service;

    @Autowired
    private TestEntityManager entityManager;

    private long chargerIdCounter = 1L;
    private long connectionIdCounter = 1L;

    private long persistChargerNearKrakow(double latOffset, ConnectorType... connectorTypes) {
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
                        .currentType(type == CCS2 || type == CHADEMO ? CurrentType.DC : CurrentType.AC_3P)
                        .build())
                .toList());
        entityManager.persistAndFlush(charger);
        return charger.getId();
    }

    private User persistUser(String email) {
        return entityManager.persist(User.builder().email(email).name("name").surname("sur").password("1234").build());
    }

    private long persistVehicle(User owner, EnumSet<VehicleInlet> inlets) {
        Vehicle vehicle = entityManager.persistAndFlush(aVehicleOwnedBy(owner).supportedInlets(inlets).build());
        return vehicle.getId();
    }

    private List<Long> chargerIds(Long vehicleId, String ownerEmail) {
        return service.getAllChargersInProximity(KRAKOW_LAT, KRAKOW_LON, RADIUS_KM, null, vehicleId, ownerEmail)
                .stream()
                .map(ChargerPointResponse::id)
                .toList();
    }

    @Test
    void withoutVehicleId_returnsSameResultsForAnonymousAndAuthenticatedUser() {
        persistUser(OWNER_EMAIL);
        long ccs2 = persistChargerNearKrakow(0.01, CCS2);
        long chademo = persistChargerNearKrakow(0.02, CHADEMO);

        assertThat(chargerIds(null, null)).containsExactly(ccs2, chademo);
        assertThat(chargerIds(null, OWNER_EMAIL)).containsExactly(ccs2, chademo);
    }

    @Test
    void withOwnedVehicle_returnsCompatibleCharger_andExcludesIncompatible() {
        long vehicleId = persistVehicle(persistUser(OWNER_EMAIL), EnumSet.of(VehicleInlet.CHADEMO));
        persistChargerNearKrakow(0.01, CCS2);
        long chademo = persistChargerNearKrakow(0.02, CHADEMO);

        assertThat(chargerIds(vehicleId, OWNER_EMAIL)).containsExactly(chademo);
    }

    @Test
    void ccs2Vehicle_seesType2AcChargers() {
        long vehicleId = persistVehicle(persistUser(OWNER_EMAIL), EnumSet.of(VehicleInlet.CCS2));
        long type2 = persistChargerNearKrakow(0.01, TYPE_2);
        long type2SocketOnly = persistChargerNearKrakow(0.02, TYPE_2_SOCKET_ONLY);
        persistChargerNearKrakow(0.03, CHADEMO);

        assertThat(chargerIds(vehicleId, OWNER_EMAIL)).containsExactly(type2, type2SocketOnly);
    }

    @Test
    void vehicleWithoutInlets_returnsNoChargers() {
        long vehicleId = persistVehicle(persistUser(OWNER_EMAIL), EnumSet.noneOf(VehicleInlet.class));
        persistChargerNearKrakow(0.01, CCS2);

        assertThat(chargerIds(vehicleId, OWNER_EMAIL)).isEmpty();
    }

    @Test
    void anonymousUserWithVehicleId_isRejected() {
        long vehicleId = persistVehicle(persistUser(OWNER_EMAIL), EnumSet.of(VehicleInlet.CCS2));

        assertThatThrownBy(() -> chargerIds(vehicleId, null))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    void otherUsersVehicle_isRejectedSameAsMissingVehicle() {
        long vehicleId = persistVehicle(persistUser(OWNER_EMAIL), EnumSet.of(VehicleInlet.CCS2));
        persistUser(OTHER_EMAIL);

        Throwable otherUsersVehicle = catchThrowable(() -> chargerIds(vehicleId, OTHER_EMAIL));
        Throwable missingVehicle = catchThrowable(() -> chargerIds(vehicleId + 1000, OTHER_EMAIL));

        assertThat(otherUsersVehicle).isInstanceOf(EntityNotFoundException.class);
        assertThat(missingVehicle).isInstanceOf(EntityNotFoundException.class);
        assertThat(otherUsersVehicle).hasMessage(missingVehicle.getMessage());
    }
}
