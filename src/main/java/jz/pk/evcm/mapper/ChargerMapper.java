package jz.pk.evcm.mapper;

import jz.pk.evcm.dto.req.ocm.AddressInfoDto;
import jz.pk.evcm.dto.req.ocm.ChargerPointDto;
import jz.pk.evcm.dto.req.ocm.ConnectionDto;
import jz.pk.evcm.entity.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChargerMapper {

    ChargerPoint toEntity(ChargerPointDto dto);

    @Mapping(target = "location", expression = "java(createPoint(dto.latitude(), dto.longitude()))")
    AddressInfo addressInfoDtoToAddressInfo(AddressInfoDto dto);

    default Connection mapConnection(ConnectionDto dto) {
        if (dto == null)
            return null;

        Connection connection = new Connection();
        connection.setId(dto.id());
        connection.setAmps(dto.amps());
        connection.setVoltage(dto.voltage());
        connection.setPowerKW(dto.powerKW());
        connection.setQuantity(dto.quantity());

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
                case TYPE_2 -> CurrentType.AC_3P;
                default -> CurrentType.UNKNOWN;
            };
        }
        connection.setCurrentType(validCurrentType);

        return connection;
    }

    default Point createPoint(Double latitude, Double longitude) {
        if (latitude == null || longitude == null)
            return null;

        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }
}