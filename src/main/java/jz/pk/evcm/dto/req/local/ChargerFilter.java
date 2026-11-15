package jz.pk.evcm.dto.req.local;

public record ChargerFilter(CurrentKind currentType, Boolean operationalOnly) {

    public static final ChargerFilter NONE = new ChargerFilter(null, null);

    public boolean isOperationalOnly() {
        return Boolean.TRUE.equals(operationalOnly);
    }
}
