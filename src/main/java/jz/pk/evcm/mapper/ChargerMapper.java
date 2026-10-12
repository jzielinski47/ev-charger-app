package jz.pk.evcm.mapper;

import jz.pk.evcm.dto.req.ocm.AddressInfoDto;
import jz.pk.evcm.dto.req.ocm.ChargerPointRequest;
import jz.pk.evcm.dto.req.ocm.ConnectionDto;
import jz.pk.evcm.entity.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ChargerMapper {
    GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    ChargerPoint toEntity(ChargerPointRequest dto);

    @Mapping(target = "location", expression = "java(createPoint(dto.latitude(), dto.longitude()))")
    AddressInfo addressInfoDtoToAddressInfo(AddressInfoDto dto);

    default Connection mapConnection(ConnectionDto dto) {
        if (dto == null)
            return null;

        Connection connection =
                Connection.builder()
                        .id(dto.id())
                        .amps(dto.amps())
                        .voltage(dto.voltage())
                        .powerKW(dto.powerKW())
                        .quantity(dto.quantity())
                        .build();

        ConnectorType mappedConnector = dto.connectionType() != null
                ? ConnectorType.fromTitle(dto.connectionType().title())
                : ConnectorType.UNKNOWN;
        connection.setConnectorType(mappedConnector);

        CurrentType validCurrentType;
        if (dto.currentType() != null && dto.currentType().title() != null) {
            validCurrentType = CurrentType.fromTitle(dto.currentType().title());
        } else {
            validCurrentType = switch (mappedConnector) {
                case CHADEMO, CCS2 -> CurrentType.DC;
                case TYPE_2, TYPE_2_TETHERED, TYPE_2_SOCKET_ONLY -> CurrentType.AC_3P;
                default -> CurrentType.UNKNOWN;
            };
        }
        connection.setCurrentType(validCurrentType);

        return connection;
    }

    default Point createPoint(Double latitude, Double longitude) {
        if (latitude == null || longitude == null)
            return null;

        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }

    @AfterMapping
    default void linkConnections(@MappingTarget ChargerPoint chargerPoint) {
        if (chargerPoint.getConnections() != null) {
            chargerPoint.getConnections().forEach(conn -> conn.setChargerPoint(chargerPoint));
        }
    }
}