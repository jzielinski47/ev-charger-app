package jz.pk.evcm.dto.res;

import jz.pk.evcm.entity.Vehicle;

import java.time.Year;

public record VehicleDTO(
        Long id,
        String brand,
        String model,
        Year yearOfProduction,
        Long ownerId
) {
    public VehicleDTO(Vehicle vehicle) {
        this(
                vehicle.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYearOfProduction(),
                vehicle.getOwner() != null ? vehicle.getOwner().getId() : null
        );
    }
}
