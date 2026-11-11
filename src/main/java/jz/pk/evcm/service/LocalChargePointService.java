package jz.pk.evcm.service;

import jakarta.persistence.EntityNotFoundException;
import jz.pk.evcm.dto.res.ChargerPointResponse;
import jz.pk.evcm.entity.ChargerPoint;
import jz.pk.evcm.entity.ConnectorType;
import jz.pk.evcm.entity.Vehicle;
import jz.pk.evcm.entity.VehicleInlet;
import jz.pk.evcm.repository.ChargerPointRepository;
import jz.pk.evcm.repository.VehicleRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.domain.Limit;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class LocalChargePointService {

    private static final int DEFAULT_MAX_RESULTS = 100;
    private static final double KM_PER_DEGREE_LAT = 111.32;
    private static final double MAX_LATITUDE_FOR_COS = 89.0;

    private final ChargerPointRepository chargerPointRepository;
    private final VehicleRepository vehicleRepository;
    // set the same coordinate system as GPS (WGS84)
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public LocalChargePointService(ChargerPointRepository chargerPointRepository,
                                   VehicleRepository vehicleRepository) {
        this.chargerPointRepository = chargerPointRepository;
        this.vehicleRepository = vehicleRepository;
    }

    public ChargerPointResponse getChargerPointById(Long id) {
        return chargerPointRepository.findById(id)
                .map(ChargerPointResponse::fromChargerPoint)
                .orElseThrow(() -> new EntityNotFoundException("Charger point not found"));
    }

    public List<ChargerPointResponse> getAllChargersInProximity(Double latitude, Double longitude,
                                                                Double distanceInKm) {
        return getAllChargersInProximity(latitude, longitude, distanceInKm, DEFAULT_MAX_RESULTS, null, null);
    }

    public List<ChargerPointResponse> getAllChargersInProximity(Double latitude, Double longitude,
                                                                Double distanceInKm, Integer maxResults,
                                                                Long vehicleId, String ownerEmail) {
        Point userLocation = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        double distanceDeg = kmToDegrees(distanceInKm, latitude);
        double distanceMeters = distanceInKm * 1000;
        Limit limit = Limit.of(maxResults != null ? maxResults : DEFAULT_MAX_RESULTS);

        if (vehicleId == null) {
            return toResponses(chargerPointRepository.findNearbyChargers(
                    userLocation, distanceDeg, distanceMeters, limit));
        }

        Set<ConnectorType> connectorTypes = compatibleConnectorsOfOwnedVehicle(vehicleId, ownerEmail);
        if (connectorTypes.isEmpty()) {
            return List.of();
        }
        return toResponses(chargerPointRepository.findNearbyChargersWithConnectorTypeIn(
                userLocation, distanceDeg, distanceMeters, connectorTypes, limit));
    }

    private Set<ConnectorType> compatibleConnectorsOfOwnedVehicle(Long vehicleId, String ownerEmail) {
        if (ownerEmail == null) {
            throw new AuthenticationCredentialsNotFoundException("Vehicle filter requires authentication");
        }
        Vehicle vehicle = vehicleRepository.findByIdAndOwnerEmail(vehicleId, ownerEmail)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found"));
        return VehicleInlet.compatibleConnectors(vehicle.getSupportedInlets());
    }

    private static List<ChargerPointResponse> toResponses(List<ChargerPoint> chargers) {
        return chargers.stream()
                .map(ChargerPointResponse::fromChargerPoint)
                .toList();
    }

    private static double kmToDegrees(double km, double latitude) {
        double poleward = Math.min(Math.abs(latitude) + km / KM_PER_DEGREE_LAT, MAX_LATITUDE_FOR_COS);
        return km / (KM_PER_DEGREE_LAT * Math.cos(Math.toRadians(poleward)));
    }

}
