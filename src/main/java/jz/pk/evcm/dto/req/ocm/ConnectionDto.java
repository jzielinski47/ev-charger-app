package jz.pk.evcm.dto.req.ocm;

public record ConnectionDto(
        Long id,
        ConnectionTypeDto connectionType,
        Integer amps,
        Integer voltage,
        Double powerKW,
        Integer currentTypeId,
        CurrentType currentType,
        Integer quantity
) {
}
