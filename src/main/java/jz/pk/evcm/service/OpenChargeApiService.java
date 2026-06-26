package jz.pk.evcm.service;

import jz.pk.evcm.dto.req.ocm.ChargerPointDto;
import jz.pk.evcm.entity.ChargerPoint;
import jz.pk.evcm.entity.Connection;
import jz.pk.evcm.entity.ConnectionType;
import jz.pk.evcm.mapper.ChargerMapper;
import jz.pk.evcm.repository.ChargerPointRepository;
import jz.pk.evcm.repository.ConnectionTypeRepository;
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
    private final ConnectionTypeRepository connectionTypeRepository;
    private final ChargerPointRepository chargerPointRepository;
    private final ChargerMapper chargerMapper;
    private final String apiBaseUrl = "https://api.openchargemap.io/v3";
    RestClient restClient;

    public OpenChargeApiService(
            @Value("${opencharge.api.key}") String apiKey,
            ChargerPointRepository chargerPointRepository,
            ChargerMapper chargerMapper,
            ConnectionTypeRepository connectionTypeRepository) {
        this.restClient = RestClient.builder()
                .baseUrl(apiBaseUrl)
                .defaultHeader("X-API-Key", apiKey)
                .build();
        this.chargerPointRepository = chargerPointRepository;
        this.chargerMapper = chargerMapper;
        this.connectionTypeRepository = connectionTypeRepository;
    }

    @Override
    public List<ChargerPointDto> fetchAllChargersInProximity(Double latitude, Double longitude, Double distanceInKm,
                                                             Integer maxResults) {
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
        List<ChargerPointDto> fetchedChargers = this.fetchAllChargersInProximity(latitude, longitude, distanceInKm, maxResults);

        if (fetchedChargers != null && !fetchedChargers.isEmpty()) {
            List<ChargerPoint> chargerEntities = fetchedChargers.stream()
                    .map(chargerMapper::toEntity)
                    .toList();

            // 1. Extract and deduplicate ConnectionTypes
            List<ConnectionType> uniqueTypes = chargerEntities.stream()
                    .flatMap(charger -> charger.getConnections().stream())
                    .map(Connection::getConnectionType)
                    .filter(java.util.Objects::nonNull)
                    // This collects them into a Map using the ID as the key,
                    // automatically removing duplicates, then grabs the values.
                    .collect(java.util.stream.Collectors.toMap(
                            ConnectionType::getId,
                            type -> type,
                            (existing, replacement) -> existing
                    ))
                    .values().stream().toList();

            // 2. Save the unique types to the DB first
            // You will need to create a ConnectionTypeRepository for this
            connectionTypeRepository.saveAll(uniqueTypes);

            // 3. Now save the chargers
            chargerPointRepository.saveAll(chargerEntities);
        }

        return fetchedChargers;
    }


}