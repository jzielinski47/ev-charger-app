package jz.pk.evcm.service;

import jakarta.transaction.Transactional;
import jz.pk.evcm.dto.req.ocm.ChargerPointRequest;
import jz.pk.evcm.dto.res.ChargerImportSummary;
import jz.pk.evcm.entity.ChargerPoint;
import jz.pk.evcm.mapper.ChargerMapper;
import jz.pk.evcm.repository.ChargerPointRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * OPEN CHARGE MAP SERVICE to call the API for the list of chargers
 * docs: https://www.openchargemap.org/develop/api
 */

@Service
public class OpenChargeApiService implements OpenChargeApiContract {

    private final ChargerPointRepository chargerPointRepository;
    private final ChargerMapper chargerMapper;
    private final RestClient restClient;

    public OpenChargeApiService(
            @Value("${opencharge.api.key}") String apiKey,
            @Value("${opencharge.api.base-url:https://api.openchargemap.io/v3}") String apiBaseUrl,
            ChargerPointRepository chargerPointRepository,
            ChargerMapper chargerMapper) {
        this.restClient = RestClient.builder()
                .baseUrl(apiBaseUrl)
                .defaultHeader("X-API-Key", apiKey)
                .build();
        this.chargerPointRepository = chargerPointRepository;
        this.chargerMapper = chargerMapper;
    }

    @Override
    public List<ChargerPointRequest> fetchAllChargersInProximity(Double latitude, Double longitude, Double distanceInKm,
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
                .body(new ParameterizedTypeReference<List<ChargerPointRequest>>() {
                });
    }

    @Transactional
    public ChargerImportSummary fetchChargersAndSave(
            Double latitude,
            Double longitude,
            Double distanceInKm,
            Integer maxResults
    ) {
        List<ChargerPointRequest> fetchedChargers = this.fetchAllChargersInProximity(latitude, longitude, distanceInKm,
                maxResults);

        if (fetchedChargers == null || fetchedChargers.isEmpty()) {
            return ChargerImportSummary.EMPTY;
        }

        List<ChargerPoint> chargerEntities = fetchedChargers.stream()
                .map(chargerMapper::toEntity)
                .toList();

        List<Long> ids = chargerEntities.stream()
                .map(ChargerPoint::getId)
                .toList();

        Set<Long> existingIds = chargerPointRepository.findAllById(ids).stream()
                .map(ChargerPoint::getId)
                .collect(Collectors.toSet());

        chargerPointRepository.saveAll(chargerEntities);

        int updated = (int) ids.stream().distinct().filter(existingIds::contains).count();
        int imported = (int) ids.stream().distinct().count();
        return new ChargerImportSummary(imported, imported - updated, updated, fetchedChargers);
    }


}