package jz.pk.evcm.service;

import jakarta.persistence.EntityNotFoundException;
import jz.pk.evcm.dto.req.local.ChargerFilter;
import jz.pk.evcm.dto.req.local.ChargerSearchOptions;
import jz.pk.evcm.dto.res.ChargerPointResponse;
import jz.pk.evcm.entity.ChargerPoint;
import jz.pk.evcm.entity.ConnectorType;
import jz.pk.evcm.entity.CurrentType;
import jz.pk.evcm.entity.Vehicle;
import jz.pk.evcm.entity.VehicleInlet;
import jz.pk.evcm.exception.InvalidSearchArgumentException;
import jz.pk.evcm.repository.ChargerPointRepository;
import jz.pk.evcm.repository.ChargerSearchCriteria;
import jz.pk.evcm.repository.ChargerSearchHit;
import jz.pk.evcm.repository.VehicleRepository;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class LocalChargePointService {

    private final ChargerPointRepository chargerPointRepository;
    private final VehicleRepository vehicleRepository;

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

    public List<ChargerPointResponse> getAllChargersInProximity(double latitude, double longitude, double distanceInKm,
                                                                Long vehicleId, String ownerEmail,
                                                                ChargerSearchOptions options) {
        validatePaging(options);
        Set<ConnectorType> connectorTypes = null;
        if (vehicleId != null) {
            connectorTypes = compatibleConnectorsOfOwnedVehicle(vehicleId, ownerEmail);
            if (connectorTypes.isEmpty()) {
                return List.of();
            }
        }

        ChargerFilter filter = options.effectiveFilter();
        Set<CurrentType> currentTypes = filter.currentType() != null ? filter.currentType().currentTypes() : null;
        ChargerSearchCriteria criteria = new ChargerSearchCriteria(connectorTypes, currentTypes,
                filter.isOperationalOnly(), options.effectiveSort(), options.effectiveLimit(), options.effectiveOffset());

        return load(chargerPointRepository.searchInRadius(latitude, longitude, distanceInKm, criteria));
    }

    private static void validatePaging(ChargerSearchOptions options) {
        int limit = options.effectiveLimit();
        if (limit < 1 || limit > ChargerSearchOptions.MAX_LIMIT) {
            throw new InvalidSearchArgumentException("limit must be between 1 and " + ChargerSearchOptions.MAX_LIMIT);
        }
        if (options.effectiveOffset() < 0) {
            throw new InvalidSearchArgumentException("offset must not be negative");
        }
    }

    private Set<ConnectorType> compatibleConnectorsOfOwnedVehicle(Long vehicleId, String ownerEmail) {
        if (ownerEmail == null) {
            throw new AuthenticationCredentialsNotFoundException("Vehicle filter requires authentication");
        }
        Vehicle vehicle = vehicleRepository.findByIdAndOwnerEmail(vehicleId, ownerEmail)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found"));
        return VehicleInlet.compatibleConnectors(vehicle.getSupportedInlets());
    }

    private List<ChargerPointResponse> load(List<ChargerSearchHit> hits) {
        if (hits.isEmpty()) {
            return List.of();
        }
        List<Long> ids = hits.stream().map(ChargerSearchHit::id).toList();
        Map<Long, ChargerPoint> byId = chargerPointRepository.findAllWithDetailsByIdIn(ids).stream()
                .collect(Collectors.toMap(ChargerPoint::getId, Function.identity()));
        return hits.stream()
                .map(hit -> ChargerPointResponse.fromChargerPoint(byId.get(hit.id()), hit.distanceMeters() / 1000))
                .toList();
    }
}
