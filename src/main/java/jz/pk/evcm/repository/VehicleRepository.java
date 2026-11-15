package jz.pk.evcm.repository;

import jz.pk.evcm.entity.Vehicle;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    /** Owner is fetched in the same query: the admin list shows it for every vehicle. */
    @Override
    @EntityGraph(attributePaths = "owner")
    List<Vehicle> findAll();

    @EntityGraph(attributePaths = "owner")
    List<Vehicle> findByOwnerEmail(String email);
    Optional<Vehicle> findByIdAndOwnerEmail(Long id, String email);
}
