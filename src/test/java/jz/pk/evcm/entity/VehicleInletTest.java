package jz.pk.evcm.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Set;

import static jz.pk.evcm.entity.ConnectorType.*;
import static org.assertj.core.api.Assertions.assertThat;

class VehicleInletTest {

    @ParameterizedTest
    @EnumSource(VehicleInlet.class)
    void compatibleConnectors_forEveryInlet_matchesExpectedSet(VehicleInlet inlet) {
        assertThat(inlet.compatibleConnectors()).isEqualTo(expectedConnectors(inlet));
    }

    @ParameterizedTest
    @EnumSource(VehicleInlet.class)
    void compatibleConnectors_forEveryInlet_isNotEmpty(VehicleInlet inlet) {
        assertThat(inlet.compatibleConnectors()).isNotEmpty();
    }

    @ParameterizedTest
    @EnumSource(VehicleInlet.class)
    void compatibleConnectors_forEveryInlet_doesNotContainUnknown(VehicleInlet inlet) {
        assertThat(inlet.compatibleConnectors()).doesNotContain(UNKNOWN);
    }

    @Test
    void compatibleConnectors_forCcs2_includesAllType2Variants() {
        assertThat(VehicleInlet.CCS2.compatibleConnectors())
                .contains(CCS2, TYPE_2, TYPE_2_SOCKET_ONLY, TYPE_2_TETHERED);
    }

    @Test
    void compatibleConnectors_forMultipleInlets_returnsUnion() {
        assertThat(VehicleInlet.compatibleConnectors(List.of(VehicleInlet.CHADEMO, VehicleInlet.TYPE_2)))
                .containsExactlyInAnyOrder(CHADEMO, TYPE_2, TYPE_2_SOCKET_ONLY, TYPE_2_TETHERED);
    }

    @Test
    void compatibleConnectors_forEmptyCollection_returnsEmptySet() {
        assertThat(VehicleInlet.compatibleConnectors(List.of())).isEmpty();
    }

    private static Set<ConnectorType> expectedConnectors(VehicleInlet inlet) {
        return switch (inlet) {
            case TYPE_1 -> Set.of(TYPE_1);
            case TYPE_2 -> Set.of(TYPE_2, TYPE_2_SOCKET_ONLY, TYPE_2_TETHERED);
            case CCS2 -> Set.of(CCS2, TYPE_2, TYPE_2_SOCKET_ONLY, TYPE_2_TETHERED);
            case CHADEMO -> Set.of(CHADEMO);
            case NACS -> Set.of(NACS, TESLA);
        };
    }
}
