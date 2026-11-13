package jz.pk.evcm.dto.res;

import jz.pk.evcm.entity.AddressInfo;
import jz.pk.evcm.entity.ChargerPoint;
import jz.pk.evcm.entity.Connection;
import jz.pk.evcm.entity.DataProvider;
import jz.pk.evcm.entity.Operator;

import java.time.Instant;
import java.util.List;

public record ChargerPointResponse(
        Long id,
        AddressResponse addressInfo,
        List<ConnectionResponse> connections,
        OperatorResponse operator,
        DataProviderResponse dataProvider,
        Boolean isOperational,
        String usageCost,
        Integer numberOfPoints,
        Instant dateLastVerified,
        Instant dateLastConfirmed
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
                        : List.of(),
                OperatorResponse.fromOperator(chargerPoint.getOperator()),
                DataProviderResponse.fromDataProvider(chargerPoint.getDataProvider()),
                chargerPoint.getIsOperational(),
                chargerPoint.getUsageCost(),
                chargerPoint.getNumberOfPoints(),
                chargerPoint.getDateLastVerified(),
                chargerPoint.getDateLastConfirmed()
        );
    }
}

record AddressResponse(
        Long id,
        String title,
        String addressLine1,
        String town,
        Double latitude,
        Double longitude,
        String accessComments
) {
    public static AddressResponse fromAddressInfo(AddressInfo address) {
        if (address == null) return null;

        Double lat = null;
        Double lon = null;

        if (address.getLocation() != null) {
            lat = address.getLocation().getY();
            lon = address.getLocation().getX();
        }

        return new AddressResponse(
                address.getId(),
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

record OperatorResponse(
        Long id,
        String name,
        String websiteUrl,
        String phone,
        String email,
        String faultReportEmail
) {
    public static OperatorResponse fromOperator(Operator operator) {
        if (operator == null) {
            return null;
        }

        return new OperatorResponse(
                operator.getId(),
                operator.getName(),
                operator.getWebsiteUrl(),
                operator.getPhone(),
                operator.getEmail(),
                operator.getFaultReportEmail()
        );
    }
}

record DataProviderResponse(
        Long id,
        String license
) {
    public static DataProviderResponse fromDataProvider(DataProvider dataProvider) {
        if (dataProvider == null) {
            return null;
        }

        return new DataProviderResponse(dataProvider.getId(), dataProvider.getLicense());
    }
}
