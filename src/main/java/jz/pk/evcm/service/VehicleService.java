package jz.pk.evcm.service;

import jz.pk.evcm.dto.res.VehicleDTO;
import jz.pk.evcm.entity.Vehicle;

import java.util.List;

public interface VehicleService {

    List<VehicleDTO> getAllVehicles();

    VehicleDTO getVehicleById(Long id);

    VehicleDTO addVehicle();

    VehicleDTO modifyVehicle();

    VehicleDTO removeVehicle();

}
