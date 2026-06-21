package jz.pk.evcm.dto.req.ocm;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ConnectionTypeDto(
        @JsonProperty("ID") Long id,
        @JsonProperty("Title") String title,
        @JsonProperty("FormalName") String formalName,
        @JsonProperty("IsDiscontinued") Boolean isDiscontinued, // If true, this is an discontinued but used connection type
        @JsonProperty("IsObsolete") Boolean isObsolete // If true, this is an obsolete connection type and is unlikely top be present in modern infrastructure
) {
}