package jz.pk.evcm.dto.res;

import jz.pk.evcm.entity.Vehicle;
import jz.pk.evcm.entity.VehicleInlet;
import jz.pk.evcm.entity.VehicleOriginatingMarket;

import java.time.Year;
import java.util.Set;

public record VehicleResponse(
        Long id,
        String brand,
        String model,
        Year yearOfProduction,
        Set<VehicleInlet> supportedInlets,
        VehicleOriginatingMarket vehicleOriginatingMarket,
        Double batteryCapacityKwh,
        Double maxAcPowerKw,
        Double maxDcPowerKw,
        Integer acPhases,
        Long ownerId
) {
    public VehicleResponse(Vehicle vehicle) {
        this(
                vehicle.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYearOfProduction(),
                vehicle.getSupportedInlets() != null ? Set.copyOf(vehicle.getSupportedInlets()) : Set.of(),
                vehicle.getVehicleOriginatingMarket(),
                vehicle.getBatteryCapacityKwh(),
                vehicle.getMaxAcPowerKw(),
                vehicle.getMaxDcPowerKw(),
                vehicle.getAcPhases(),
                vehicle.getOwner() != null ? vehicle.getOwner().getId() : null
        );
    }
}
