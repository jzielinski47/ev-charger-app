package jz.pk.evcm.service;

import jakarta.persistence.EntityNotFoundException;
import jz.pk.evcm.dto.res.VehicleDTO;
import jz.pk.evcm.entity.Vehicle;
import jz.pk.evcm.repository.VehicleRepository;

import java.util.List;

public class VehicleService {

    private VehicleRepository vehicleRepository;

    List<VehicleDTO> getAllVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        return vehicles.stream().map(VehicleDTO::new).toList();
    }

    VehicleDTO getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        return new VehicleDTO(vehicle);
    }

    VehicleDTO addVehicle() {
        return null;
    }

    VehicleDTO modifyVehicle() {
        return null;
    }

    VehicleDTO removeVehicle() {
        return null;
    }

    boolean setFavouriteVehicle(Long id) {
        return true;
    }

}
