package jz.pk.evcm.service;

import jz.pk.evcm.dto.req.ocm.ChargerPointDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/*
* OPEN CHARGE MAP SERVICE to call the API for the list of chargers
* docs: https://www.openchargemap.org/develop/api
*/

@Service
public class OCMService {
    RestClient restClient;
    private final String apiBaseUrl = "https://api.openchargemap.io/v3";

    public OCMService(@Value("${OPEN_CHARGE_API_KEY}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl(apiBaseUrl)
                .defaultHeader("X-API-Key", apiKey)
                .defaultHeader("Accept", "application/json")
                .build();
    }

    // TODO: Implement OpenChargeAPI requests
    public List<ChargerPointDto> getAllChargerPointsAround(Double latitude, Long longitude) {

        return new ArrayList<>();
    }

    public List<ChargerPointDto> ocmTestEndpoint() {
        // call https://api.openchargemap.io/v3/poi/?output=json&countrycode=US&maxresults=10
       return new ArrayList<>();
    }
}