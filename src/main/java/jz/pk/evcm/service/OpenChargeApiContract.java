package jz.pk.evcm.service;

import jz.pk.evcm.dto.req.ocm.ChargerPointDto;

import java.util.List;

public interface OpenChargeApiContract {

    List<ChargerPointDto> fetchAllChargersInProximity(Double latitude, Double longitude, Double distanceInKm);
    List<ChargerPointDto> fetchAllChargersInProximity(Double latitude, Double longitude, Double distanceInKm, Integer maxResults);
}
