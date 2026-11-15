package jz.pk.evcm.service;

import jz.pk.evcm.dto.res.ChargerPointResponse;
import jz.pk.evcm.entity.AddressInfo;
import jz.pk.evcm.entity.ChargerPoint;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestContainersConfig.class, LocalChargePointService.class})
class LocalChargePointServiceSpatialTest {

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Autowired
    private LocalChargePointService service;

    @Autowired
    private TestEntityManager entityManager;

    private void persistChargerAt(long id, double longitude, double latitude) {
        AddressInfo address = new AddressInfo();
        address.setId(id);
        address.setLocation(geometryFactory.createPoint(new Coordinate(longitude, latitude)));

        ChargerPoint charger = new ChargerPoint();
        charger.setId(id);
        charger.setAddressInfo(address);
        entityManager.persistAndFlush(charger);
    }

    @Test
    void findsChargerNearEastEdgeOfRadiusAtHighLatitude() {
        persistChargerAt(1L, 1.3134943103101826, 70.00300179266618);

        List<ChargerPointResponse> results = service.getAllChargersInProximity(70.0, 0.0, 50.0, null, null);

        assertThat(results).extracting(ChargerPointResponse::id).containsExactly(1L);
    }

    @Test
    void excludesChargerJustBeyondRadius() {
        persistChargerAt(1L, 1.33, 70.0); // ~50.6 km east

        List<ChargerPointResponse> results = service.getAllChargersInProximity(70.0, 0.0, 50.0, null, null);

        assertThat(results).isEmpty();
    }
}