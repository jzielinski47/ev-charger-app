package jz.pk.evcm.service;

import jakarta.persistence.EntityNotFoundException;
import jz.pk.evcm.dto.res.VehicleResponse;
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
            if(targetUserEmail != null && !targetUserEmail.isBlank()) {
                vehicles = vehicleRepository.findByOwnerEmail(targetUserEmail);
            } else {
                vehicles = vehicleRepository.findAll();
            }
        } else {
            vehicles = vehicleRepository.findByOwnerEmail(userEmail);
        }

        return vehicles.stream().map(VehicleResponse::new).toList();
    }

    public VehicleResponse getVehicleById(Long vehicleId, String userEmail, boolean isAdmin, String targetUserEmail) {
        Vehicle vehicle;

        if(isAdmin) {
            if(targetUserEmail != null && !targetUserEmail.isBlank()) {
                vehicle = vehicleRepository.findById(vehicleId).orElseThrow(EntityNotFoundException::new);
            }
            vehicle = vehicleRepository.findById(vehicleId).orElseThrow(EntityNotFoundException::new);
        } else {
            vehicle = vehicleRepository.findById(vehicleId).orElseThrow(EntityNotFoundException::new);
        }

        return new VehicleResponse(vehicle);
    }

}
