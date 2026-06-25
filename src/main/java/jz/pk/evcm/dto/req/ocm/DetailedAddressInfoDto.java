package jz.pk.evcm.dto.req.ocm;

public record DetailedAddressInfoDto(
        Long id,
        String addressLine1,
        String addressLine2,
        String town,
        String stateOrProvince,
        String postcode
) {
}