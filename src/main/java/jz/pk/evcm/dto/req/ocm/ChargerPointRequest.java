package jz.pk.evcm.dto.req.ocm;

import java.time.Instant;
import java.util.List;

public record ChargerPointRequest(
        Long id,
        AddressInfoDto addressInfo,
        List<ConnectionDto> connections,
        OperatorInfoDto operatorInfo,
        DataProviderDto dataProvider,
        StatusTypeDto statusType,
        String usageCost,
        Integer numberOfPoints,
        Instant dateLastVerified,
        Instant dateLastConfirmed
) {
}
