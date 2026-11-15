package jz.pk.evcm.service;

import jakarta.persistence.EntityNotFoundException;
import jz.pk.evcm.dto.req.local.ChargerFilter;
import jz.pk.evcm.dto.res.ChargerPointResponse;
import jz.pk.evcm.entity.ChargerPoint;
import jz.pk.evcm.entity.ConnectorType;
import jz.pk.evcm.entity.CurrentType;
import jz.pk.evcm.entity.Vehicle;
import jz.pk.evcm.entity.VehicleInlet;
import jz.pk.evcm.repository.ChargerPointRepository;
import jz.pk.evcm.repository.ChargerSearchCriteria;
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

    private static final int MAX_RESULTS = 100;

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
                                                                ChargerFilter filter) {
        Set<ConnectorType> connectorTypes = null;
        if (vehicleId != null) {
            connectorTypes = compatibleConnectorsOfOwnedVehicle(vehicleId, ownerEmail);
            if (connectorTypes.isEmpty()) {
                return List.of();
            }
        }

        ChargerFilter effectiveFilter = filter != null ? filter : ChargerFilter.NONE;
        Set<CurrentType> currentTypes = effectiveFilter.currentType() != null
                ? effectiveFilter.currentType().currentTypes()
                : null;
        ChargerSearchCriteria criteria = new ChargerSearchCriteria(
                connectorTypes, currentTypes, effectiveFilter.isOperationalOnly(), MAX_RESULTS);

        return load(chargerPointRepository.searchInRadius(latitude, longitude, distanceInKm, criteria));
    }

    private Set<ConnectorType> compatibleConnectorsOfOwnedVehicle(Long vehicleId, String ownerEmail) {
        if (ownerEmail == null) {
            throw new AuthenticationCredentialsNotFoundException("Vehicle filter requires authentication");
        }
        Vehicle vehicle = vehicleRepository.findByIdAndOwnerEmail(vehicleId, ownerEmail)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found"));
        return VehicleInlet.compatibleConnectors(vehicle.getSupportedInlets());
    }

    private List<ChargerPointResponse> load(List<Long> orderedIds) {
        if (orderedIds.isEmpty()) {
            return List.of();
        }
        Map<Long, ChargerPoint> byId = chargerPointRepository.findAllWithDetailsByIdIn(orderedIds).stream()
                .collect(Collectors.toMap(ChargerPoint::getId, Function.identity()));
        return orderedIds.stream()
                .map(byId::get)
                .map(ChargerPointResponse::fromChargerPoint)
                .toList();
    }
}
