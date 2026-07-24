package jz.pk.evcm.service;

import jz.pk.evcm.dto.req.ocm.ChargerPointRequest;

import java.util.List;

public interface ChargerPointsAPIContract {

    List<ChargerPointRequest> fetchAllChargersInProximity(Double latitude, Double longitude, Double distanceInKm, Integer maxResults);

    default List<ChargerPointRequest> fetchAllChargersInProximity(Double latitude, Double longitude, Double distanceInKm) {
        return this.fetchAllChargersInProximity(latitude, longitude, distanceInKm, 100); // default maxResults to 100
    };
}
