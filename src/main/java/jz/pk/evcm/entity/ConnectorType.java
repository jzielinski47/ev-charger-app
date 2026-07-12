package jz.pk.evcm.entity;

public enum ConnectorType {
    TYPE_2("Type 2"),
    TYPE_2_SOCKET_ONLY("Type 2 (Socket Only)"),
    TYPE_2_TETHERED("Type 2 (Tethered Connector)"),
    CHADEMO("CHAdeMO"),
    CCS2("CCS (Type 2)"),
    TYPE_1("Type 1 (J1772)"),
    TESLA("Tesla (Model S/X)"),
    NACS("NACS / Tesla Supercharger"),
    UNKNOWN("Unknown Connector Type");

    public final String label;

    ConnectorType(String label) {
        this.label = label;
    }

    public static ConnectorType fromTitle(String title) {
        if(title == null)
            return UNKNOWN;

        for(ConnectorType type : values()) {
            if(type.label.equalsIgnoreCase(title.trim())) {
                return type;
            }
        }

        return UNKNOWN;
    }


}
