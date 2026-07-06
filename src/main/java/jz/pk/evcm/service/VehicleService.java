package jz.pk.evcm.service;

import jakarta.persistence.EntityNotFoundException;
import jz.pk.evcm.dto.req.local.InputVehicleDto;
import jz.pk.evcm.dto.res.ResVehicleDto;
import jz.pk.evcm.entity.ConnectorType;
import jz.pk.evcm.entity.User;
import jz.pk.evcm.entity.Vehicle;
import jz.pk.evcm.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public List<ResVehicleDto> getAllVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        return vehicles.stream().map(ResVehicleDto::new).toList();
    }

    public ResVehicleDto getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        return new ResVehicleDto(vehicle);
    }

    public ResVehicleDto addVehicle(User owner, InputVehicleDto dto) {
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

    public ResVehicleDto modifyVehicle(InputVehicleDto dto) {
        return null;
    }

    public ResVehicleDto removeVehicle(Long id) {
        return null;
    }

    public boolean setFavouriteVehicle(Long id) {
        return true;
    }

}
