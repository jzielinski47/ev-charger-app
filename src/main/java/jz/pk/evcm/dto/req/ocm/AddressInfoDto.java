package jz.pk.evcm.dto.req.ocm;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AddressInfoDto(
        @JsonProperty("ID") Long id,
        @JsonProperty("Title") String title,
        @JsonProperty("AddressLine1") String addressLine1,
        @JsonProperty("AddressLine2") String addressLine2,
        @JsonProperty("Town") String town,
        @JsonProperty("StateOrProvince") String stateOrProvince,
        @JsonProperty("Postcode") String postcode,
        @JsonProperty("Latitude") Double latitude,
        @JsonProperty("Longitude") Double longitude,
        @JsonProperty("AccessComments") String accessComments
) {
}