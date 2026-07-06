package jz.pk.evcm.dto.res;

import jz.pk.evcm.entity.ConnectorType;
import jz.pk.evcm.entity.Vehicle;

import java.time.Year;

public record VehicleResponse(
        Long id,
        String brand,
        String model,
        Year yearOfProduction,
        Long ownerId,
        ConnectorType connectorType,
        boolean isConnectorModified
) {
    public VehicleResponse(Vehicle vehicle) {
        this(
                vehicle.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYearOfProduction(),
                vehicle.getOwner() != null ? vehicle.getOwner().getId() : null,
                vehicle.getConnector(),
                vehicle.isConnectorModified()
        );
    }
}
