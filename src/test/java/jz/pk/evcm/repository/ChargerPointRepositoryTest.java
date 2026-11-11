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
import org.springframework.data.domain.Limit;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestContainersConfig.class)
class ChargerPointRepositoryTest {

    private static final double COARSE_RADIUS_DEG = 1.0;
    private static final double TEN_KM = 10_000;

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
        void findsChargerInsideRadius() {
            ChargerPoint inside = createChargerAt(0.0, 0.05); // ~5.6 km

            List<ChargerPoint> results = repository.findNearbyChargers(
                    searchCenter, COARSE_RADIUS_DEG, TEN_KM, Limit.of(10));

            assertThat(results).containsExactly(inside);
        }

        @Test
        void ignoresChargerOutsideRadiusInMeters() {
            createChargerAt(0.0, 0.1); // ~11.1 km

            List<ChargerPoint> results = repository.findNearbyChargers(
                    searchCenter, COARSE_RADIUS_DEG, TEN_KM, Limit.of(10));

            assertThat(results).isEmpty();
        }

        @Test
        void returnsChargersOrderedByDistance() {
            ChargerPoint far = createChargerAt(0.0, 0.08);   // ~8.9 km
            ChargerPoint near = createChargerAt(0.01, 0.0);  // ~1.1 km
            ChargerPoint middle = createChargerAt(0.0, -0.04); // ~4.4 km
            createChargerAt(0.2, 0.0); // ~22 km

            List<ChargerPoint> results = repository.findNearbyChargers(
                    searchCenter, COARSE_RADIUS_DEG, TEN_KM, Limit.of(10));

            assertThat(results).containsExactly(near, middle, far);
        }

        @Test
        void respectsLimitAndKeepsTheClosest() {
            ChargerPoint closest = createChargerAt(0.0, 0.01);
            ChargerPoint second = createChargerAt(0.0, 0.02);
            createChargerAt(0.0, 0.03);

            List<ChargerPoint> results = repository.findNearbyChargers(
                    searchCenter, COARSE_RADIUS_DEG, TEN_KM, Limit.of(2));

            assertThat(results).containsExactly(closest, second);
        }
    }

    @Nested
    class SpatialIndexTests {

        @Test
        void flywayCreatedGistIndexOnLocation() {
            Object indexDef = entityManager.getEntityManager()
                    .createNativeQuery("SELECT indexdef FROM pg_indexes WHERE indexname = 'idx_address_info_location'")
                    .getSingleResult();

            assertThat(indexDef.toString()).containsIgnoringCase("USING gist (location)");
        }

        @Test
        void dwithinPrefilterCanUseGistIndex() {
            entityManager.getEntityManager().createNativeQuery("SET LOCAL enable_seqscan = off").executeUpdate();

            @SuppressWarnings("unchecked")
            List<String> plan = entityManager.getEntityManager().createNativeQuery("""
                    EXPLAIN SELECT * FROM address_info
                    WHERE ST_DWithin(location, ST_SetSRID(ST_MakePoint(19.94, 50.06), 4326), 0.5)
                    """).getResultList();

            assertThat(String.join("\n", plan)).contains("idx_address_info_location");
        }
    }
}
