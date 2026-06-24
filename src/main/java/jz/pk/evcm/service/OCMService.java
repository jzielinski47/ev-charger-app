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
public class OCMService {
    private final String apiBaseUrl = "https://api.openchargemap.io/v3";
    RestClient restClient;

    public OCMService(@Value("${opencharge.api.key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl(apiBaseUrl)
                .defaultHeader("X-API-Key", apiKey)
//                .defaultHeader("Accept", "application/json")
                .build();
    }

    public String test() {
        String req =
                String.valueOf(restClient.get()
                        .uri(uriBuilder -> uriBuilder.path("/poi").queryParam("output",
                                "json").queryParam("countrycode", "US").queryParam("maxresults", 10).build())
                        .retrieve()
                        .body(String.class));

        // call https://api.openchargemap.io/v3/poi/?output=json&countrycode=US&maxresults=10
        return req;
    }

    public List<ChargerPointDto> getAllChargerPointsAround(Double latitude, Double longitude) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/poi")
                        .queryParam("output", "json")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("distance", 10) // Example: 10km radius
                        .queryParam("distanceunit", "km")
                        .queryParam("camelcase", "true")
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<ChargerPointDto>>() {
                });
    }

    public List<ChargerPointDto> ocmTestEndpoint() {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/poi")
                        .queryParam("output", "json")
                        .queryParam("countrycode", "US")
                        .queryParam("maxresults", "10")
                        .queryParam("camelcase", "true")
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<ChargerPointDto>>() {
                });
    }
}