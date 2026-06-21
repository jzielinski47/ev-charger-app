package jz.pk.evcm.dto.req.ocm;

public record ConnectionTypeDto(
        Long id,
        String title,
        String formalName,
        Boolean isDiscontinued, // If true, this is an discontinued but used connection type
        Boolean isObsolete // If true, this is an obsolete connection type and is unlikely top be present in modern infrastructure
) {
}
