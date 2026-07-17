package jz.pk.evcm.service;

import jz.pk.evcm.dto.res.ChargerPointResponse;
import jz.pk.evcm.repository.ChargerPointRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocalChargePointService {
    private final ChargerPointRepository chargerPointRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    public LocalChargePointService(ChargerPointRepository chargerPointRepository) {
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

        Point userLocation = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        userLocation.setSRID(4326); // set the same coordinate system as GPS (WGS84)
        return chargerPointRepository.findNearbyChargers(userLocation, distanceInKm)
                .stream().map(ChargerPointResponse::fromChargerPoint)
                .collect(Collectors.toList());
    }


}
