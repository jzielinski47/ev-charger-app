package jz.pk.evcm.service;

import jz.pk.evcm.dto.req.ocm.ChargerPointDto;
import jz.pk.evcm.entity.AddressInfo;
import jz.pk.evcm.entity.ChargerPoint;
import jz.pk.evcm.entity.Connection;
import jz.pk.evcm.repository.ChargerPointRepository;
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
    private final ChargerPointRepository chargerPointRepository;
    private final String apiBaseUrl = "https://api.openchargemap.io/v3";
    RestClient restClient;

    public OpenChargeApiService(@Value("${opencharge.api.key}") String apiKey, ChargerPointRepository chargerPointRepository) {
        this.restClient = RestClient.builder()
                .baseUrl(apiBaseUrl)
                .defaultHeader("X-API-Key", apiKey)
                .build();
        this.chargerPointRepository = chargerPointRepository;
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

    public List<ChargerPointDto> fetchChargersAndSave(Double latitude, Double longitude, Double distanceInKm, Integer maxResults) {
        List<ChargerPointDto> chargers = this.fetchAllChargersInProximity(latitude, longitude, distanceInKm,
                maxResults);



//        chargerPointRepository.saveAll(chargers);

        return chargers;
    }



}