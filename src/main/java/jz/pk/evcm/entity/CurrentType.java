package jz.pk.evcm.entity;

public enum CurrentType {
    DC("DC"),
    AC_3P("AC (Three-Phase)"),
    AC_1P("AC (Single-Phase)"),
    UNKNOWN("Unknown Current Type");

    public final String label;

    CurrentType(String label) {
        this.label = label;
    }

    public static CurrentType fromTitle(String title) {
        if(title == null)
            return UNKNOWN;

        for(CurrentType type : values()) {
            if(type.label.equalsIgnoreCase(title.trim())) {
                return type;
            }
        }

        return UNKNOWN;
    }


}
