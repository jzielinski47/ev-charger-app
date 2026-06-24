package jz.pk.evcm.dto.req.ocm;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ShortAddressInfoDto(
        @JsonProperty("ID") Long id,
        @JsonProperty("Latitude") Double latitude,
        @JsonProperty("Longitude") Double longitude
) {
}