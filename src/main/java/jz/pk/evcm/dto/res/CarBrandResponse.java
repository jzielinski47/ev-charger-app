package jz.pk.evcm.dto.res;

import jz.pk.evcm.entity.CarBrand;

public record CarBrandResponse(String slug, String name) {
    public CarBrandResponse(CarBrand brand) {
        this(brand.getSlug(), brand.getName());
    }
}
