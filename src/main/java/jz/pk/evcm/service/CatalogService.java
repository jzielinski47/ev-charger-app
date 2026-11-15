package jz.pk.evcm.service;

import jakarta.persistence.EntityNotFoundException;
import jz.pk.evcm.dto.res.CarBrandResponse;
import jz.pk.evcm.dto.res.CarModelResponse;
import jz.pk.evcm.repository.CarBrandRepository;
import jz.pk.evcm.repository.CarModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogService {

    private final CarBrandRepository carBrandRepository;
    private final CarModelRepository carModelRepository;

    public List<CarBrandResponse> getBrands() {
        return carBrandRepository.findAllByOrderByNameAsc().stream()
                .map(CarBrandResponse::new)
                .toList();
    }

    public List<CarModelResponse> getModels(String brandSlug) {
        if (!carBrandRepository.existsBySlug(brandSlug)) {
            throw new EntityNotFoundException("Brand not found");
        }
        return carModelRepository.findByBrandSlugOrderByNameAsc(brandSlug).stream()
                .map(CarModelResponse::new)
                .toList();
    }
}
