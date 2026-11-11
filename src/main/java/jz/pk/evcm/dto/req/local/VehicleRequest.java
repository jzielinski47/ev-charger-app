package jz.pk.evcm.dto.req.local;

import jakarta.validation.constraints.NotEmpty;
import jz.pk.evcm.entity.VehicleInlet;

import java.time.Year;
import java.util.Set;

public record VehicleRequest(
        String brand,
        String model,
        Year yearOfProduction,
        @NotEmpty Set<VehicleInlet> supportedInlets
) {}
