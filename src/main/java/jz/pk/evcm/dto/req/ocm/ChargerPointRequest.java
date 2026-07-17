package jz.pk.evcm.dto.req.ocm;

import java.util.List;

public record ChargerPointRequest(
        Long id,
        AddressInfoDto addressInfo,
        List<ConnectionDto> connections
) {
}

record AddressInfoDto(
        Long id,
        String title,
        String addressLine1,
        String addressLine2,
        String town,
        String stateOrProvince,
        String postcode,
        Double latitude,
        Double longitude,
        String accessComments
) {
}

record ConnectionDto(
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

record ConnectionTypeDto(
        Long id,
        String title,
        String formalName,
        Boolean isDiscontinued, // If true, this is an discontinued but used connection type
        Boolean isObsolete // If true, this is an obsolete connection type and is unlikely to be present in modern infrastructure
) {
}

record CurrentType(
        Integer id,
        String title
) {
}
