package jz.pk.evcm.repository;

import jz.pk.evcm.entity.ConnectorType;
import jz.pk.evcm.entity.CurrentType;

import java.util.Set;

public record ChargerSearchCriteria(
        Set<ConnectorType> connectorTypes,
        Set<CurrentType> currentTypes,
        boolean operationalOnly,
        int limit
) {
}
