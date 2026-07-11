package jz.pk.evcm.service;

import jz.pk.evcm.dto.res.ChargerPointResponse;
import jz.pk.evcm.repository.ChargerPointRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocalChargerPointService {
    private final ChargerPointRepository chargerPointRepository;

    public LocalChargerPointService(ChargerPointRepository chargerPointRepository) {
        this.chargerPointRepository = chargerPointRepository;
    }

    public List<ChargerPointResponse> getAllChargersInProximity(
            Double latitude,
            Double longitude,
            Double distanceInKm
    ) {
        return getAllChargersInProximity(latitude, longitude, distanceInKm, 100);
    }

    public List<ChargerPointResponse> getAllChargersInProximity(
            Double latitude,
            Double longitude,
            Double distanceInKm,
            Integer maxResults
    ) {
        return List.of();
    }


}
