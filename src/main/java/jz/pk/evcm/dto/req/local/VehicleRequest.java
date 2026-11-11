package jz.pk.evcm.dto.req.local;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jz.pk.evcm.entity.VehicleInlet;
import jz.pk.evcm.entity.VehicleOriginatingMarket;

import java.time.Year;
import java.util.Set;

public record VehicleRequest(
        String brand,
        String model,
        Year yearOfProduction,
        @NotEmpty Set<@NotNull VehicleInlet> supportedInlets,
        VehicleOriginatingMarket vehicleOriginatingMarket,
        @Positive Double batteryCapacityKwh,
        @Positive Double maxAcPowerKw,
        @Positive Double maxDcPowerKw,
        @Min(1) @Max(3) Integer acPhases
) {}
