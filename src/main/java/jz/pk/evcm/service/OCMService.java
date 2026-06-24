package jz.pk.evcm.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/*
* OPEN CHARGE MAP SERVICE to call the API for the list of chargers
* docs: https://www.openchargemap.org/develop/api
*/

@Service
public class OCMService {
    private final RestClient restClient;
    private final String apiBaseUrl = "https://api.openchargemap.io/v3";
    private final String apiKey;

    public OCMService(
            RestClient.Builder restClientBuilder,
            @Value("${OPEN_CHARGE_API_KEY}") String apiKey) {

        this.apiKey = apiKey;
        this.restClient = restClientBuilder
                .baseUrl(apiBaseUrl)
                .defaultHeader("X-API-Key", this.apiKey)
                .build();
    }

    // TODO: Implement OpenChargeAPI requests
}