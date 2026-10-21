package jz.pk.evcm.repository;

import jz.pk.evcm.entity.AddressInfo;
import jz.pk.evcm.entity.ChargerPoint;
import jz.pk.evcm.testdata.TestContainersConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestContainersConfig.class)
class ChargerPointRepositoryTest {

    @Autowired
    private ChargerPointRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private GeometryFactory geometryFactory;

    private long idCounter = 1L;

    @BeforeEach
    void setUp() {
        geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    }

    private ChargerPoint createChargerAt(double longitude, double latitude) {
        Point location = geometryFactory.createPoint(new Coordinate(longitude, latitude));

        AddressInfo addressInfo = new AddressInfo();
        addressInfo.setId(idCounter);
        addressInfo.setLocation(location);

        ChargerPoint chargerPoint = new ChargerPoint();
        chargerPoint.setId(idCounter++);
        chargerPoint.setAddressInfo(addressInfo);

        return entityManager.persistFlushFind(chargerPoint);
    }

    @Nested
    class FindNearbyChargersSpatialTests {

        private Point searchCenter;

        @BeforeEach
        void setUpSearchCenter() {
            searchCenter = geometryFactory.createPoint(new Coordinate(0.0, 0.0));
        }

        @Test
        void findsChargerStrictlyInsideRadius() {
            ChargerPoint insideCharger = createChargerAt(0.0, 5.0);

            List<ChargerPoint> results = repository.findNearbyChargers(searchCenter, 10.0);

            assertThat(results).containsExactly(insideCharger);
        }

        @Test
        void findsChargerExactlyOnRadiusEdge() {
            ChargerPoint edgeCharger = createChargerAt(0.0, 10.0);

            List<ChargerPoint> results = repository.findNearbyChargers(searchCenter, 10.0);

            assertThat(results).containsExactly(edgeCharger);
        }

        @Test
        void ignoresChargerOutsideRadius() {
            createChargerAt(0.0, 10.1);

            List<ChargerPoint> results = repository.findNearbyChargers(searchCenter, 10.0);

            assertThat(results).isEmpty();
        }

        @Test
        void filtersCorrectlyWhenMultipleChargersExistAtVariousDistances() {
            ChargerPoint inside = createChargerAt(0.0, 3.0);
            ChargerPoint edge = createChargerAt(0.0, 10.0);
            createChargerAt(0.0, 15.0);
            createChargerAt(20.0, 0.0);

            List<ChargerPoint> results = repository.findNearbyChargers(searchCenter, 10.0);

            assertThat(results).containsExactlyInAnyOrder(inside, edge);
        }
    }
}