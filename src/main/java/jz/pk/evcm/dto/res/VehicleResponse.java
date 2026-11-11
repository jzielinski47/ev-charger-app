package jz.pk.evcm.dto.res;

import jz.pk.evcm.entity.Vehicle;
import jz.pk.evcm.entity.VehicleInlet;

import java.time.Year;
import java.util.Set;

public record VehicleResponse(
        Long id,
        String brand,
        String model,
        Year yearOfProduction,
        Set<VehicleInlet> supportedInlets,
        Long ownerId
) {
    public VehicleResponse(Vehicle vehicle) {
        this(
                vehicle.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYearOfProduction(),
                vehicle.getSupportedInlets() != null ? Set.copyOf(vehicle.getSupportedInlets()) : Set.of(),
                vehicle.getOwner() != null ? vehicle.getOwner().getId() : null
        );
    }
}
