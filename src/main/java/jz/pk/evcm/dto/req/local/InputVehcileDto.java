package jz.pk.evcm.dto.req.local;

import java.time.Year;

public record InputVehcileDto(
        Long id,
        String brand,
        String model,
        Year yearOfProduction,
        String connector,
        Boolean isConnectorModified
) {}
