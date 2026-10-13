package jz.pk.evcm.testdata;

import jz.pk.evcm.entity.ConnectorType;
import jz.pk.evcm.entity.User;
import jz.pk.evcm.entity.Vehicle;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

public final class TestVehicleDefaults {

    public static Vehicle.VehicleBuilder aVehicle() {
        return Vehicle.builder()
                .brand("Tesla")
                .model("Model Y")
                .yearOfProduction(Year.of(2026))
                .supportedConnectorTypes(new ArrayList<>(List.of(ConnectorType.CCS2)));
    }

    public static Vehicle.VehicleBuilder aVehicleOwnedBy(User owner) {
        return aVehicle().owner(owner);
    }

}
