package jz.pk.evcm.service;

import jz.pk.evcm.dto.req.ocm.ChargerPointDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

/*
 * OPEN CHARGE MAP SERVICE to call the API for the list of chargers
 * docs: https://www.openchargemap.org/develop/api
 */

@Service
public class OpenChargeApiService implements OpenChargeApiContract {
    private final String apiBaseUrl = "https://api.openchargemap.io/v3";
    RestClient restClient;

    public OpenChargeApiService(@Value("${opencharge.api.key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl(apiBaseUrl)
                .defaultHeader("X-API-Key", apiKey)
                .build();
    }

    @Override
    public List<ChargerPointDto> fetchAllChargersInProximity(Double latitude, Double longitude, Double distanceInKm) {
        return this.fetchAllChargersInProximity(latitude, longitude, distanceInKm, 100);
    }

    @Override
    public List<ChargerPointDto> fetchAllChargersInProximity(Double latitude, Double longitude, Double distanceInKm, Integer maxResults) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/poi")
                        .queryParam("output", "json")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("distance", distanceInKm)
                        .queryParam("distanceunit", "km")
                        .queryParam("camelcase", "true")
                        .queryParam("maxresults", maxResults)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<ChargerPointDto>>() {
                });
    }
}