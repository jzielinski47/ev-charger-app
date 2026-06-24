package jz.pk.evcm.dto.req.ocm;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ConnectionDto(
        @JsonProperty("ID") Long id,
        @JsonProperty("ConnectionType") ConnectionTypeDto connectionType,
        @JsonProperty("Amps") Integer amps,
        @JsonProperty("Voltage") Integer voltage,
        @JsonProperty("PowerKW") Double powerKW,
        @JsonProperty("CurrentTypeID") Integer currentTypeId,
        @JsonProperty("Quantity") Integer quantity
) {
}