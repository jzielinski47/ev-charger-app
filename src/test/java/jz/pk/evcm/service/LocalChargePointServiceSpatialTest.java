package jz.pk.evcm.service;

import jz.pk.evcm.dto.req.local.ChargerSearchOptions;
import jz.pk.evcm.dto.res.ChargerPointResponse;
import jz.pk.evcm.entity.AddressInfo;
import jz.pk.evcm.entity.ChargerPoint;
import jz.pk.evcm.exception.InvalidSearchArgumentException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        List<ChargerPointResponse> results = service.getAllChargersInProximity(70.0, 0.0, 50.0, null, null, ChargerSearchOptions.DEFAULTS);

        assertThat(results).extracting(ChargerPointResponse::id).containsExactly(1L);
    }

    @Test
    void excludesChargerJustBeyondRadius() {
        persistChargerAt(1L, 1.33, 70.0); // ~50.6 km east

        List<ChargerPointResponse> results = service.getAllChargersInProximity(70.0, 0.0, 50.0, null, null, ChargerSearchOptions.DEFAULTS);

        assertThat(results).isEmpty();
    }

    @Test
    void rejectsOutOfRangeCoordinatesAndNonPositiveRadius() {
        assertThatThrownBy(() -> service.getAllChargersInProximity(91.0, 0.0, 5.0, null, null, ChargerSearchOptions.DEFAULTS))
                .isInstanceOf(InvalidSearchArgumentException.class)
                .hasMessage("lat must be between -90 and 90");
        assertThatThrownBy(() -> service.getAllChargersInProximity(0.0, -180.5, 5.0, null, null, ChargerSearchOptions.DEFAULTS))
                .hasMessage("lon must be between -180 and 180");
        assertThatThrownBy(() -> service.getAllChargersInProximity(0.0, 0.0, 0.0, null, null, ChargerSearchOptions.DEFAULTS))
                .hasMessage("proximity must be greater than 0");
    }

    @Test
    void findsChargersInBounds() {
        persistChargerAt(1L, 19.95, 50.06);
        persistChargerAt(2L, 20.5, 50.06);

        List<ChargerPointResponse> results = service.getAllChargersInBounds(50.1, 50.0, 20.0, 19.9,
                null, null, ChargerSearchOptions.DEFAULTS);

        assertThat(results).extracting(ChargerPointResponse::id).containsExactly(1L);
    }

    @Test
    void rejectsInvertedOrAntimeridianCrossingBounds() {
        assertThatThrownBy(() -> service.getAllChargersInBounds(50.0, 50.1, 20.0, 19.9, null, null, ChargerSearchOptions.DEFAULTS))
                .isInstanceOf(InvalidSearchArgumentException.class)
                .hasMessage("south must not be greater than north");
        assertThatThrownBy(() -> service.getAllChargersInBounds(10.0, -10.0, -170.0, 170.0, null, null, ChargerSearchOptions.DEFAULTS))
                .hasMessage("west must not be greater than east");
        assertThatThrownBy(() -> service.getAllChargersInBounds(95.0, 0.0, 10.0, 0.0, null, null, ChargerSearchOptions.DEFAULTS))
                .hasMessage("north must be between -90 and 90");
    }
}
