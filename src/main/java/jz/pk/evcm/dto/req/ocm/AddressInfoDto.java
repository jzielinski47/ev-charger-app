package jz.pk.evcm.dto.req.ocm;

public record AddressInfoDto(
        Long id,
        String title,
        String addressLine1,
        String addressLine2,
        String town,
        String stateOrProvince,
        String postcode,
        Double latitude,
        Double longitude,
        String accessComments
) {
}