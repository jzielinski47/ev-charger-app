package jz.pk.evcm.repository;

import jz.pk.evcm.entity.ChargerPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ChargerPointRepository extends JpaRepository<ChargerPoint, Long>, ChargerPointSearchRepository {

    @Query("""
            SELECT cp FROM ChargerPoint cp
            JOIN FETCH cp.addressInfo
            LEFT JOIN FETCH cp.operator
            LEFT JOIN FETCH cp.dataProvider
            WHERE cp.id IN :ids
            """)
    List<ChargerPoint> findAllWithDetailsByIdIn(@Param("ids") Collection<Long> ids);
}
