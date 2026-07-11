package jz.pk.evcm.mapper;

import jz.pk.evcm.dto.req.ocm.AddressInfoDto;
import jz.pk.evcm.dto.req.ocm.ChargerPointDto;
import jz.pk.evcm.entity.AddressInfo;
import jz.pk.evcm.entity.ChargerPoint;
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

    default Point createPoint(Double latitude, Double longitude) {
        if (latitude == null || longitude == null)
            return null;

        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }
}