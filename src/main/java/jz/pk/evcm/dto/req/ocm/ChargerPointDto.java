package jz.pk.evcm.dto.req.ocm;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ChargerPointDto(
        @JsonProperty("ID") Long id,
        @JsonProperty("AddressInfo") AddressInfoDto addressInfo,
        @JsonProperty("Connections") List<ConnectionDto> connections
) {
}