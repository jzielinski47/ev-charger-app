package jz.pk.evcm.testdata;

import jz.pk.evcm.entity.User;
import jz.pk.evcm.entity.Vehicle;
import jz.pk.evcm.entity.VehicleInlet;

import java.time.Year;
import java.util.EnumSet;

public final class TestVehicleDefaults {

    public static Vehicle.VehicleBuilder aVehicle() {
        return Vehicle.builder()
                .brand("Tesla")
                .model("Model Y")
                .yearOfProduction(Year.of(2026))
                .supportedInlets(EnumSet.of(VehicleInlet.CCS2));
    }

    public static Vehicle.VehicleBuilder aVehicleOwnedBy(User owner) {
        return aVehicle().owner(owner);
    }

}
