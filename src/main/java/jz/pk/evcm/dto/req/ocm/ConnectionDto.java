package jz.pk.evcm.dto.req.ocm;

import jz.pk.evcm.entity.PowerSupplyType;

public record ConnectionDto(
        Long id,
        ConnectionTypeDto connectionType,
        Integer amps,
        Integer voltage,
        Double powerKW,
        PowerSupplyType powerSupplyType,
        Integer quantity
) {
}
