package jz.pk.evcm.repository;

import jz.pk.evcm.entity.ConnectorType;
import jz.pk.evcm.entity.Vehicle;

import java.time.Year;
import java.util.List;

public class VehicleTestFactory {

    public static Vehicle.VehicleBuilder baseVehicleBuilder() {
        return Vehicle.builder()
                .brand("Tesla")
                .model("Model Y")
                .yearOfProduction(Year.of(2026))
                .supportedConnectorTypes(List.of(ConnectorType.CCS2));
    }
}
