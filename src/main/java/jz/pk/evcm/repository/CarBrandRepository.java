package jz.pk.evcm.repository;

import jz.pk.evcm.entity.CarBrand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarBrandRepository extends JpaRepository<CarBrand, Long> {

    List<CarBrand> findAllByOrderByNameAsc();

    boolean existsBySlug(String slug);
}
