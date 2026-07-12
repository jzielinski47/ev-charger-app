package jz.pk.evcm.dto.req.local;

import jz.pk.evcm.entity.ConnectorType;

import java.time.Year;
import java.util.List;

public record VehicleRequest(
        String brand,
        String model,
        Year yearOfProduction,
        List<ConnectorType> supportedConnectorTypes
) {}
