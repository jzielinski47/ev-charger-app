package jz.pk.evcm.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.util.Collection;
import java.util.List;
import java.util.Set;

class ChargerPointSearchRepositoryImpl implements ChargerPointSearchRepository {

    private static final double KM_PER_DEGREE_LAT = 111.32;
    private static final double MAX_LATITUDE_FOR_COS = 89.0;
    private static final List<String> NO_VALUES = List.of("");

    private static final String REFERENCE_POINT = "ST_SetSRID(ST_MakePoint(:refLon, :refLat), 4326)";

    private static final String SELECT = """
            SELECT cp.charger_point_id
            FROM charger_point cp
            JOIN address_info a ON a.id = cp.address_info_id
            CROSS JOIN LATERAL (
                SELECT COUNT(*) AS matching
                FROM connection c
                WHERE c.charger_point_id = cp.charger_point_id
                  AND (:anyConnector OR c.connector_type IN (:connectorTypes))
                  AND (:anyCurrent OR c.current_type IN (:currentTypes))
            ) m
            WHERE (:operationalOnly = FALSE OR cp.is_operational = TRUE)
              AND ((:anyConnector AND :anyCurrent) OR m.matching > 0)
            """;

    private static final String IN_RADIUS = """
              AND ST_DWithin(a.location, %1$s, :distanceDeg)
              AND ST_DistanceSphere(a.location, %1$s) <= :distanceMeters
            """.formatted(REFERENCE_POINT);

    private static final String ORDER_AND_PAGE = """
            ORDER BY ST_DistanceSphere(a.location, %s), cp.charger_point_id
            LIMIT :limit
            """.formatted(REFERENCE_POINT);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Long> searchInRadius(double latitude, double longitude, double distanceInKm,
                                     ChargerSearchCriteria criteria) {
        Query query = entityManager.createNativeQuery(SELECT + IN_RADIUS + ORDER_AND_PAGE, Long.class)
                .setParameter("distanceDeg", kmToDegrees(distanceInKm, latitude))
                .setParameter("distanceMeters", distanceInKm * 1000);
        return run(query, latitude, longitude, criteria);
    }

    @SuppressWarnings("unchecked")
    private static List<Long> run(Query query, double refLatitude, double refLongitude,
                                  ChargerSearchCriteria criteria) {
        return query
                .setParameter("refLat", refLatitude)
                .setParameter("refLon", refLongitude)
                .setParameter("anyConnector", criteria.connectorTypes() == null)
                .setParameter("connectorTypes", names(criteria.connectorTypes()))
                .setParameter("anyCurrent", criteria.currentTypes() == null)
                .setParameter("currentTypes", names(criteria.currentTypes()))
                .setParameter("operationalOnly", criteria.operationalOnly())
                .setParameter("limit", criteria.limit())
                .getResultList();
    }

    private static Collection<String> names(Set<? extends Enum<?>> values) {
        if (values == null || values.isEmpty()) {
            return NO_VALUES;
        }
        return values.stream().map(Enum::name).toList();
    }

    private static double kmToDegrees(double km, double latitude) {
        double poleward = Math.min(Math.abs(latitude) + km / KM_PER_DEGREE_LAT, MAX_LATITUDE_FOR_COS);
        return km / (KM_PER_DEGREE_LAT * Math.cos(Math.toRadians(poleward)));
    }
}
