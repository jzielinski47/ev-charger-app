package jz.pk.evcm.repository;

import jz.pk.evcm.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByOwnerEmail(String email);
    Optional<Vehicle> findByIdAndOwnerEmail(Long id, String email);
}
