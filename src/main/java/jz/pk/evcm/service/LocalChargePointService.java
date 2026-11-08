package jz.pk.evcm.service;

import jz.pk.evcm.dto.res.ChargerPointResponse;
import jz.pk.evcm.repository.ChargerPointRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocalChargePointService {

    private static final int DEFAULT_MAX_RESULTS = 100;
    private static final double KM_PER_DEGREE_LAT = 111.32;

    private final ChargerPointRepository chargerPointRepository;
    // set the same coordinate system as GPS (WGS84)
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

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

    @Transactional(readOnly = true)
    public List<ChargerPointResponse> getAllChargersInProximity(Double latitude, Double longitude,
                                                                Double distanceInKm, Integer maxResults) {
        Point userLocation = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        int limit = maxResults != null ? maxResults : DEFAULT_MAX_RESULTS;

        return chargerPointRepository.findNearbyChargers(
                        userLocation,
                        kmToDegrees(distanceInKm, latitude),
                        distanceInKm * 1000,
                        Limit.of(limit))
                .stream()
                .map(ChargerPointResponse::fromChargerPoint)
                .toList();
    }

    private static double kmToDegrees(double km, double latitude) {
        double cosLat = Math.cos(Math.toRadians(latitude));
        return km / (KM_PER_DEGREE_LAT * Math.max(cosLat, 0.01));
    }

}
