package jz.pk.evcm.dto.res;

import jz.pk.evcm.entity.ChargerPoint;
import jz.pk.evcm.entity.Connection;

import java.util.List;

public record ChargerPointResponse(
        Long id,
        AddressResponse address,
        List<ConnectionResponse> connections
) {
    public static ChargerPointResponse fromChargerPoint(ChargerPoint chargerPoint) {
        if (chargerPoint == null) {
            return null;
        }

        return new ChargerPointResponse(
                chargerPoint.getId(),
                AddressResponse.fromAddressInfo(chargerPoint.getAddressInfo()),
                chargerPoint.getConnections() != null
                        ? chargerPoint.getConnections().stream()
                        .map(ConnectionResponse::fromConnection)
                        .toList()
                        : List.of()
        );
    }
}

record AddressResponse(
        String title,
        String addressLine1,
        String town,
        Double latitude,
        Double longitude,
        String accessComments
) {
    public static AddressResponse fromAddressInfo(jz.pk.evcm.entity.AddressInfo address) {
        if (address == null) return null;

        Double lat = null;
        Double lon = null;

        if (address.getLocation() != null) {
            lat = address.getLocation().getY();
            lon = address.getLocation().getX();
        }

        return new AddressResponse(
                address.getTitle(),
                address.getAddressLine1(),
                address.getTown(),
                lat,
                lon,
                address.getAccessComments()
        );
    }
}

record ConnectionResponse(
        Long id,
        Integer amps,
        Integer voltage,
        Double powerKW,
        Integer quantity,
        String connectorType,
        String currentType
) {
    public static ConnectionResponse fromConnection(Connection connection) {
        if (connection == null) {
            return null;
        }

        return new ConnectionResponse(
                connection.getId(),
                connection.getAmps(),
                connection.getVoltage(),
                connection.getPowerKW(),
                connection.getQuantity(),
                String.valueOf(connection.getConnectorType()),
                String.valueOf(connection.getCurrentType())
        );
    }
}

