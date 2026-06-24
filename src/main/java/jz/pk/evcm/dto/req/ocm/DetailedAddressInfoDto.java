package jz.pk.evcm.dto.req.ocm;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DetailedAddressInfoDto(
        @JsonProperty("ID") Long id,
        @JsonProperty("AddressLine1") String addressLine1,
        @JsonProperty("AddressLine2") String addressLine2,
        @JsonProperty("Town") String town,
        @JsonProperty("StateOrProvince") String stateOrProvince,
        @JsonProperty("Postcode") String postcode
) {
}