package jz.pk.evcm.dto.req.ocm;

public record ConnectionTypeDto(
        Long id,
        String title,
        String formalName,
        Boolean isDiscontinued,
        Boolean isObsolete
) {
}
