package jz.pk.evcm.mapper;

import jz.pk.evcm.dto.req.ocm.ChargerPointDto;
import jz.pk.evcm.entity.ChargerPoint;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChargerMapper {

    ChargerPoint toEntity(ChargerPointDto dto);

}