package jz.pk.evcm.repository;

import jz.pk.evcm.entity.CarModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarModelRepository extends JpaRepository<CarModel, Long> {

    List<CarModel> findByBrandSlugOrderByNameAsc(String slug);
}
