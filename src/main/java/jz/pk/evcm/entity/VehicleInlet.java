package jz.pk.evcm.entity;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public enum VehicleInlet {
    TYPE_1(Set.of(ConnectorType.TYPE_1)),
    TYPE_2(Set.of(ConnectorType.TYPE_2, ConnectorType.TYPE_2_SOCKET_ONLY, ConnectorType.TYPE_2_TETHERED)),
    CCS2(Set.of(ConnectorType.CCS2, ConnectorType.TYPE_2, ConnectorType.TYPE_2_SOCKET_ONLY, ConnectorType.TYPE_2_TETHERED)),
    CHADEMO(Set.of(ConnectorType.CHADEMO)),
    NACS(Set.of(ConnectorType.NACS, ConnectorType.TESLA));

    private final Set<ConnectorType> compatibleConnectors;

    VehicleInlet(Set<ConnectorType> compatibleConnectors) {
        this.compatibleConnectors = compatibleConnectors;
    }

    public Set<ConnectorType> compatibleConnectors() {
        return compatibleConnectors;
    }

    public static Set<ConnectorType> compatibleConnectors(Collection<VehicleInlet> inlets) {
        return inlets.stream()
                .flatMap(inlet -> inlet.compatibleConnectors.stream())
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(ConnectorType.class)));
    }
}
