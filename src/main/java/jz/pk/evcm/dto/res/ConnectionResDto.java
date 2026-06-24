package jz.pk.evcm.dto.res;

import jz.pk.evcm.entity.PowerSupplyType;

public record ConnectionResDto(
        Long id,
        ConnectionTypeResDto connectionType,
        Integer amps,
        Integer voltage,
        Double powerKW,
        PowerSupplyType powerSupplyType,
        Integer quantity
) {
}