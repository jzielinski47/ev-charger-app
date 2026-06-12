package jz.pk.evcm.service;

import jz.pk.evcm.dto.res.VehicleDTO;
import jz.pk.evcm.entity.Vehicle;

import java.util.List;

public interface VehicleService {

    List<VehicleDTO> getAllVehicles();

    Vehicle getVehicleById(Long id);

    Vehicle addVehicle();

    Vehicle modifyVehicle();

    Vehicle removeVehicle();

}
