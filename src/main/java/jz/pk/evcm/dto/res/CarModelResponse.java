package jz.pk.evcm.dto.res;

import jz.pk.evcm.entity.CarModel;
import jz.pk.evcm.entity.VehicleInlet;

import java.util.Set;

public record CarModelResponse(
        Long id,
        String name,
        Set<VehicleInlet> supportedInlets,
        Double batteryCapacityKwh,
        Double maxAcPowerKw,
        Double maxDcPowerKw,
        Integer acPhases
) {
    public CarModelResponse(CarModel model) {
        this(
                model.getId(),
                model.getName(),
                Set.copyOf(model.getSupportedInlets()),
                model.getBatteryCapacityKwh(),
                model.getMaxAcPowerKw(),
                model.getMaxDcPowerKw(),
                model.getAcPhases()
        );
    }
}
