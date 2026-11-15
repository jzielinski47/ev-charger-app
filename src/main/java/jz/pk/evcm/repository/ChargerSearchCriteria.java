package jz.pk.evcm.repository;

import jz.pk.evcm.dto.req.local.ChargerSort;
import jz.pk.evcm.entity.ConnectorType;
import jz.pk.evcm.entity.CurrentType;

import java.util.Set;

public record ChargerSearchCriteria(
        Set<ConnectorType> connectorTypes,
        Set<CurrentType> currentTypes,
        boolean operationalOnly,
        ChargerSort sort,
        int limit,
        int offset
) {
}
