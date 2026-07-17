package jz.pk.evcm.repository;

import jz.pk.evcm.entity.ChargerPoint;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ChargerPointRepository extends JpaRepository<ChargerPoint, Long> {

    @Query("SELECT cp FROM ChargerPoint cp " +
            "LEFT JOIN FETCH cp.connections " +
            "LEFT JOIN FETCH cp.addressInfo " +
            "WHERE function('ST_DWithin', cp.addressInfo.location, :userLocation, :distance) = true")
    List<ChargerPoint> findNearbyChargers(@Param("userLocation") Point userLocation, @Param("distance") double distance);
}
