package jz.pk.evcm.service;

import jz.pk.evcm.dto.req.ocm.ChargerPointDto;

import java.util.List;

public interface OpenChargeApiContract {

    List<ChargerPointDto> fetchAllChargersInProximity(Double latitude, Double longitude, Double distanceInKm, Integer maxResults);

    default List<ChargerPointDto> fetchAllChargersInProximity(Double latitude, Double longitude, Double distanceInKm) {
        return this.fetchAllChargersInProximity(latitude, longitude, distanceInKm, 100); // default maxResults to 100
    };
}
