package jz.pk.evcm.dto.req.ocm;

import java.util.List;

public record ChargerPointDto(
        Long id,
        AddressInfoDto addressInfo,
        List<ConnectionDto> connections
) {
}