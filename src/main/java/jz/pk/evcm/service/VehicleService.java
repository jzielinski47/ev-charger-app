package jz.pk.evcm.service;

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
        return null;
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
