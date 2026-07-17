package jz.pk.evcm.dto.req.ocm;

import java.util.List;

public record ChargerPointRequest(
        Long id,
        AddressInfoDto addressInfo,
        List<ConnectionDto> connections
) {
}

