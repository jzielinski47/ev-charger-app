package jz.pk.evcm.service;

import jakarta.persistence.EntityNotFoundException;
import jz.pk.evcm.dto.req.local.VehicleRequest;
import jz.pk.evcm.dto.res.VehicleResponse;
import jz.pk.evcm.entity.ConnectorType;
import jz.pk.evcm.entity.User;
import jz.pk.evcm.entity.Vehicle;
import jz.pk.evcm.repository.UserRepository;
import jz.pk.evcm.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    public VehicleService(VehicleRepository vehicleRepository, UserRepository userRepository) {
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
    }

    public List<VehicleResponse> getAllVehicles(String userEmail, boolean isAdmin, String targetUserEmail) {
        List<Vehicle> vehicles;

        if (isAdmin) {
            if (targetUserEmail != null && !targetUserEmail.isBlank()) {
                vehicles = vehicleRepository.findByOwnerEmail(targetUserEmail);
            } else {
                vehicles = vehicleRepository.findAll();
            }
        } else {
            vehicles = vehicleRepository.findByOwnerEmail(userEmail);
        }

        return vehicles.stream().map(VehicleResponse::new).toList();
    }

    public VehicleResponse getVehicleById(Long vehicleId, String currentUserEmail, boolean isAdmin) {
        Vehicle vehicle = getAccessibleVehicle(vehicleId, currentUserEmail, isAdmin);
        return new VehicleResponse(vehicle);
    }

    public VehicleResponse addVehicle(
            VehicleRequest dto,
            String currentUserEmail,
            boolean isAdmin,
            String targetUserEmail) {

        boolean isAdminCreatingOnBehalf = isAdmin && targetUserEmail != null && !targetUserEmail.isBlank();
        String ownerEmail = isAdminCreatingOnBehalf ? targetUserEmail : currentUserEmail;

        User owner = userRepository.findByEmail(ownerEmail).orElseThrow(EntityNotFoundException::new);

        Vehicle newVehicle = new Vehicle();
        newVehicle.setBrand(dto.brand());
        newVehicle.setModel(dto.model());
        newVehicle.setYearOfProduction(dto.yearOfProduction());
        newVehicle.setConnector(mapConnectorType(dto.connector()));
        newVehicle.setConnectorModified(dto.isConnectorModified());
        newVehicle.setOwner(owner);

        Vehicle savedVehicle = vehicleRepository.save(newVehicle);
        return new VehicleResponse(savedVehicle);
    }

    public VehicleResponse modifyVehicle(Long vehicleId, VehicleRequest dto, String currentUserEmail, boolean isAdmin) {

        Vehicle vehicle = getAccessibleVehicle(vehicleId, currentUserEmail, isAdmin);

        vehicle.setBrand(dto.brand());
        vehicle.setModel(dto.model());
        vehicle.setYearOfProduction(dto.yearOfProduction());
        vehicle.setConnector(mapConnectorType(dto.connector()));
        vehicle.setConnectorModified(dto.isConnectorModified());

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return new VehicleResponse(updatedVehicle);
    }

    public void deleteVehicle(Long vehicleId, String userEmail, boolean isAdmin) {
        Vehicle vehicle = getAccessibleVehicle(vehicleId, userEmail, isAdmin);
        vehicleRepository.delete(vehicle);
    }

    private Vehicle getAccessibleVehicle(Long vehicleId, String currentUserEmail, boolean isAdmin) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(EntityNotFoundException::new);

        if (isAdmin)
            return vehicle;

        if (vehicle.getOwner() == null) {
            throw new EntityNotFoundException();
        }

        if (!vehicle.getOwner().getEmail().equals(currentUserEmail)) {
            throw new EntityNotFoundException();
        }

        return vehicle;
    }

    private ConnectorType mapConnectorType(String connector) {
        if (connector == null) {
            return ConnectorType.UNKNOWN;
        }

        try {
            return ConnectorType.valueOf(connector);
        } catch (IllegalArgumentException e) {
            return ConnectorType.UNKNOWN;
        }
    }

}
