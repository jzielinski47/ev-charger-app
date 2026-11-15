package jz.pk.evcm.dto.req.local;

import jz.pk.evcm.entity.CurrentType;

import java.util.Set;

public enum CurrentKind {
    AC(Set.of(CurrentType.AC_1P, CurrentType.AC_3P)),
    DC(Set.of(CurrentType.DC));

    private final Set<CurrentType> currentTypes;

    CurrentKind(Set<CurrentType> currentTypes) {
        this.currentTypes = currentTypes;
    }

    public Set<CurrentType> currentTypes() {
        return currentTypes;
    }
}
