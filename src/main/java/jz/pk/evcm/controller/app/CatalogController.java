package jz.pk.evcm.controller.app;

import jz.pk.evcm.dto.res.CarBrandResponse;
import jz.pk.evcm.dto.res.CarModelResponse;
import jz.pk.evcm.service.CatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/brands")
    public List<CarBrandResponse> getBrands() {
        return catalogService.getBrands();
    }

    @GetMapping("/brands/{slug}/models")
    public List<CarModelResponse> getModels(@PathVariable String slug) {
        return catalogService.getModels(slug);
    }
}
