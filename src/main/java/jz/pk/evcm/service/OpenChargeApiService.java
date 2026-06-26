package jz.pk.evcm.service;

import jz.pk.evcm.dto.req.ocm.ChargerPointDto;
import jz.pk.evcm.entity.AddressInfo;
import jz.pk.evcm.entity.ChargerPoint;
import jz.pk.evcm.entity.Connection;
import jz.pk.evcm.entity.ConnectionType;
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

    // TEMPORARY UNTIL I FIND A BETTER SOLUTION
    private ChargerPoint mapToEntity(ChargerPointDto dto) {
        if (dto == null) {
            return null;
        }

        ChargerPoint chargerPoint = new ChargerPoint();
        chargerPoint.setId(dto.id());

        if (dto.addressInfo() != null) {
            AddressInfo addressInfo = new AddressInfo();
            addressInfo.setId(dto.addressInfo().id());
            addressInfo.setTitle(dto.addressInfo().title());
            addressInfo.setAddressLine1(dto.addressInfo().addressLine1());
            addressInfo.setAddressLine2(dto.addressInfo().addressLine2());
            addressInfo.setTown(dto.addressInfo().town());
            addressInfo.setStateOrProvince(dto.addressInfo().stateOrProvince());
            addressInfo.setPostcode(dto.addressInfo().postcode());
            addressInfo.setLatitude(dto.addressInfo().latitude());
            addressInfo.setLongitude(dto.addressInfo().longitude());
            addressInfo.setAccessComments(dto.addressInfo().accessComments());

            chargerPoint.setAddressInfo(addressInfo);
        }

        if (dto.connections() != null) {
            List<Connection> connections = dto.connections().stream()
                    .filter(java.util.Objects::nonNull)
                    .map(connDto -> {
                        Connection connection = new Connection();
                        connection.setId(connDto.id());
                        connection.setAmps(connDto.amps());
                        connection.setVoltage(connDto.voltage());
                        connection.setPowerKW(connDto.powerKW());
                        connection.setCurrentTypeId(connDto.currentTypeId());
                        connection.setQuantity(connDto.quantity());

                        if (connDto.connectionType() != null) {
                            ConnectionType type = new ConnectionType();
                            type.setId(connDto.connectionType().id());
                            type.setTitle(connDto.connectionType().title());
                            type.setFormalName(connDto.connectionType().formalName());
                            type.setIsDiscontinued(connDto.connectionType().isDiscontinued());
                            type.setIsObsolete(connDto.connectionType().isObsolete());
                            connection.setConnectionType(type);
                        }

                        return connection;
                    })
                    .toList();

            chargerPoint.setConnections(connections);
        }

        return chargerPoint;
    }



}