package jz.pk.evcm.dto.res;

import java.util.List;

public record DetailedChargerPointDto(
        Long id,
        String title,
        String addressLine1,
        String addressLine2,
        String town,
        String postcode,
        String accessComments,
        Double latitude,
        Double longitude,
        List<ConnectionResDto> connections
) {
}