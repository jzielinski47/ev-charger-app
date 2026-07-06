package jz.pk.evcm.service;

import jakarta.persistence.EntityNotFoundException;
import jz.pk.evcm.dto.req.local.InputVehcileDto;
import jz.pk.evcm.dto.res.ResVehicleDto;
import jz.pk.evcm.entity.ConnectorType;
import jz.pk.evcm.entity.User;
import jz.pk.evcm.entity.Vehicle;
import jz.pk.evcm.repository.VehicleRepository;

import java.util.List;

public class VehicleService {

    private VehicleRepository vehicleRepository;

    List<ResVehicleDto> getAllVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        return vehicles.stream().map(ResVehicleDto::new).toList();
    }

    ResVehicleDto getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        return new ResVehicleDto(vehicle);
    }

    ResVehicleDto addVehicle(User owner, InputVehcileDto dto) {
        Vehicle newVehicle = new Vehicle();
        newVehicle.setBrand(dto.brand());
        newVehicle.setModel(dto.model());
        newVehicle.setYearOfProduction(dto.yearOfProduction());
        newVehicle.setConnector(ConnectorType.valueOf(dto.connector()));
        newVehicle.setConnectorModified(dto.isConnectorModified());
        newVehicle.setOwner(owner);

        Vehicle savedVehicle = vehicleRepository.save(newVehicle);
        return new ResVehicleDto(savedVehicle);
    }

    ResVehicleDto modifyVehicle() {
        return null;
    }

    ResVehicleDto removeVehicle() {
        return null;
    }

    boolean setFavouriteVehicle(Long id) {
        return true;
    }

}
