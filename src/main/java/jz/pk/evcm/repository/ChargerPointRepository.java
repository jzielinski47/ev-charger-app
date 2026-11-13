package jz.pk.evcm.repository;

import jz.pk.evcm.entity.ChargerPoint;
import jz.pk.evcm.entity.ConnectorType;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ChargerPointRepository extends JpaRepository<ChargerPoint, Long> {

    /*
     * Spatial Query custom construction
     * */
    @Query("""
            SELECT cp FROM ChargerPoint cp
            JOIN FETCH cp.addressInfo a
            LEFT JOIN FETCH cp.operator
            LEFT JOIN FETCH cp.dataProvider
            WHERE function('ST_DWithin', a.location, :userLocation, :distanceDeg) = true
              AND function('ST_DistanceSphere', a.location, :userLocation) <= :distanceMeters
            ORDER BY function('ST_DistanceSphere', a.location, :userLocation)
            """)
    List<ChargerPoint> findNearbyChargers(@Param("userLocation") Point userLocation,
                                          @Param("distanceDeg") double distanceDeg,
                                          @Param("distanceMeters") double distanceMeters,
                                          Limit limit);

    @Query("""
            SELECT cp FROM ChargerPoint cp
            JOIN FETCH cp.addressInfo a
            LEFT JOIN FETCH cp.operator
            LEFT JOIN FETCH cp.dataProvider
            WHERE function('ST_DWithin', a.location, :userLocation, :distanceDeg) = true
              AND function('ST_DistanceSphere', a.location, :userLocation) <= :distanceMeters
              AND EXISTS (SELECT 1 FROM Connection c
                          WHERE c.chargerPoint = cp AND c.connectorType IN :connectorTypes)
            ORDER BY function('ST_DistanceSphere', a.location, :userLocation)
            """)
    List<ChargerPoint> findNearbyChargersWithConnectorTypeIn(@Param("userLocation") Point userLocation,
                                                             @Param("distanceDeg") double distanceDeg,
                                                             @Param("distanceMeters") double distanceMeters,
                                                             @Param("connectorTypes") Collection<ConnectorType> connectorTypes,
                                                             Limit limit);


}
