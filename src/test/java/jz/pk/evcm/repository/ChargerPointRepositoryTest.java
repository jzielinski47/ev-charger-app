package jz.pk.evcm.repository;

import jz.pk.evcm.entity.AddressInfo;
import jz.pk.evcm.entity.ChargerPoint;
import jz.pk.evcm.entity.Connection;
import jz.pk.evcm.entity.ConnectorType;
import jz.pk.evcm.entity.CurrentType;
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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static jz.pk.evcm.entity.ConnectorType.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestContainersConfig.class)
class ChargerPointRepositoryTest {

    private static final double TEN_KM = 10.0;

    @Autowired
    private ChargerPointRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private GeometryFactory geometryFactory;

    private long idCounter = 1L;
    private long connectionIdCounter = 1L;

    @BeforeEach
    void setUp() {
        geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    }

    private ChargerPoint createChargerAt(double longitude, double latitude, ConnectorType... connectorTypes) {
        Point location = geometryFactory.createPoint(new Coordinate(longitude, latitude));

        AddressInfo addressInfo = new AddressInfo();
        addressInfo.setId(idCounter);
        addressInfo.setLocation(location);

        ChargerPoint chargerPoint = new ChargerPoint();
        chargerPoint.setId(idCounter++);
        chargerPoint.setAddressInfo(addressInfo);
        chargerPoint.setConnections(Arrays.stream(connectorTypes)
                .map(type -> Connection.builder()
                        .id(connectionIdCounter++)
                        .chargerPoint(chargerPoint)
                        .connectorType(type)
                        .currentType(type == CCS2 || type == CHADEMO ? CurrentType.DC : CurrentType.AC_3P)
                        .build())
                .toList());

        return entityManager.persistFlushFind(chargerPoint);
    }

    private List<Long> search(ChargerSearchCriteria criteria) {
        entityManager.flush();
        entityManager.clear();
        return repository.searchInRadius(0.0, 0.0, TEN_KM, criteria);
    }

    private static ChargerSearchCriteria anyCharger(int limit) {
        return new ChargerSearchCriteria(null, null, false, limit);
    }

    private static ChargerSearchCriteria withConnectors(Set<ConnectorType> connectorTypes, int limit) {
        return new ChargerSearchCriteria(connectorTypes, null, false, limit);
    }

    private static List<Long> ids(ChargerPoint... chargers) {
        return Arrays.stream(chargers).map(ChargerPoint::getId).toList();
    }

    @Nested
    class SearchInRadiusSpatialTests {

        @Test
        void findsChargerInsideRadius() {
            ChargerPoint inside = createChargerAt(0.0, 0.05); // ~5.6 km

            assertThat(search(anyCharger(10))).containsExactlyElementsOf(ids(inside));
        }

        @Test
        void ignoresChargerOutsideRadiusInMeters() {
            createChargerAt(0.0, 0.1); // ~11.1 km

            assertThat(search(anyCharger(10))).isEmpty();
        }

        @Test
        void returnsChargersOrderedByDistance() {
            ChargerPoint far = createChargerAt(0.0, 0.08);   // ~8.9 km
            ChargerPoint near = createChargerAt(0.01, 0.0);  // ~1.1 km
            ChargerPoint middle = createChargerAt(0.0, -0.04); // ~4.4 km
            createChargerAt(0.2, 0.0); // ~22 km

            assertThat(search(anyCharger(10))).containsExactlyElementsOf(ids(near, middle, far));
        }

        @Test
        void respectsLimitAndKeepsTheClosest() {
            ChargerPoint closest = createChargerAt(0.0, 0.01);
            ChargerPoint second = createChargerAt(0.0, 0.02);
            createChargerAt(0.0, 0.03);

            assertThat(search(anyCharger(2))).containsExactlyElementsOf(ids(closest, second));
        }
    }

    @Nested
    class ConnectorTypeFilterTests {

        @Test
        void returnsCompatibleCharger_andExcludesIncompatibleAndWithoutConnections() {
            ChargerPoint compatible = createChargerAt(0.0, 0.01, CCS2);
            createChargerAt(0.0, 0.02, CHADEMO);
            createChargerAt(0.0, 0.03);

            assertThat(search(withConnectors(Set.of(CCS2), 10))).containsExactlyElementsOf(ids(compatible));
        }

        @Test
        void returnsChargerWithMixedConnectorsOnce_withAllItsConnections() {
            ChargerPoint mixed = createChargerAt(0.0, 0.01, CCS2, CHADEMO, TYPE_2);

            List<Long> found = search(withConnectors(Set.of(CCS2, TYPE_2), 10));

            assertThat(found).containsExactlyElementsOf(ids(mixed));
            assertThat(repository.findAllWithDetailsByIdIn(found).getFirst().getConnections())
                    .extracting(Connection::getConnectorType)
                    .containsExactlyInAnyOrder(CCS2, CHADEMO, TYPE_2);
        }

        @Test
        void returnsCompatibleChargersOrderedByDistance() {
            ChargerPoint far = createChargerAt(0.0, 0.08, CCS2);
            ChargerPoint near = createChargerAt(0.01, 0.0, TYPE_2);
            ChargerPoint middle = createChargerAt(0.0, -0.04, CCS2);
            createChargerAt(0.005, 0.0, CHADEMO);

            assertThat(search(withConnectors(Set.of(CCS2, TYPE_2), 10)))
                    .containsExactlyElementsOf(ids(near, middle, far));
        }

        @Test
        void limitCountsChargersNotConnections() {
            ChargerPoint closest = createChargerAt(0.0, 0.01, CCS2, TYPE_2, TYPE_2_TETHERED);
            ChargerPoint second = createChargerAt(0.0, 0.02, CCS2);
            createChargerAt(0.0, 0.03, CCS2);

            assertThat(search(withConnectors(Set.of(CCS2, TYPE_2, TYPE_2_TETHERED), 2)))
                    .containsExactlyElementsOf(ids(closest, second));
        }

        @Test
        void ignoresCompatibleChargerOutsideRadiusInMeters() {
            createChargerAt(0.0, 0.1, CCS2);

            assertThat(search(withConnectors(Set.of(CCS2), 10))).isEmpty();
        }
    }

    @Nested
    class CurrentAndOperationalFilterTests {

        @Test
        void currentTypeFilter_keepsChargersWithAMatchingConnection() {
            ChargerPoint dc = createChargerAt(0.0, 0.01, CCS2);
            ChargerPoint mixed = createChargerAt(0.0, 0.02, TYPE_2, CHADEMO);
            createChargerAt(0.0, 0.03, TYPE_2);

            assertThat(search(new ChargerSearchCriteria(null, Set.of(CurrentType.DC), false, 10)))
                    .containsExactlyElementsOf(ids(dc, mixed));
        }

        @Test
        void connectorAndCurrentFilters_mustMatchTheSameConnection() {
            createChargerAt(0.0, 0.01, TYPE_2, CHADEMO);
            ChargerPoint dcCcs = createChargerAt(0.0, 0.02, CCS2);

            assertThat(search(new ChargerSearchCriteria(Set.of(TYPE_2, CCS2), Set.of(CurrentType.DC), false, 10)))
                    .containsExactlyElementsOf(ids(dcCcs));
        }

        @Test
        void operationalOnly_excludesNonOperationalAndUnknownChargers() {
            ChargerPoint operational = createChargerAt(0.0, 0.01);
            operational.setIsOperational(true);
            ChargerPoint broken = createChargerAt(0.0, 0.02);
            broken.setIsOperational(false);
            createChargerAt(0.0, 0.03);

            assertThat(search(new ChargerSearchCriteria(null, null, true, 10)))
                    .containsExactlyElementsOf(ids(operational));
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
