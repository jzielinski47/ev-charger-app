package jz.pk.evcm.dto.res;

import jz.pk.evcm.entity.Vehicle;

import java.time.Year;

public record ResVehicleDto(
        Long id,
        String brand,
        String model,
        Year yearOfProduction,
        Long ownerId
) {
    public ResVehicleDto(Vehicle vehicle) {
        this(
                vehicle.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYearOfProduction(),
                vehicle.getOwner() != null ? vehicle.getOwner().getId() : null
        );
    }
}
