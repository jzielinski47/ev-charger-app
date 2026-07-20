package jz.pk.evcm.service;

import jz.pk.evcm.dto.res.VehicleResponse;
import jz.pk.evcm.entity.UserRole;
import jz.pk.evcm.entity.Vehicle;
import jz.pk.evcm.exception.ForbiddenAccessException;
import jz.pk.evcm.repository.VehicleRepository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static reactor.core.publisher.Mono.when;

@ExtendWith(MockitoExtension.class)
public class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleService vehicleService;

    @Test
    public void VehicleService_GetAllVehiclesAsAdmin_ReturnsAllVehicles() {
        
        String adminEmail = "admin@test.invalid";
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setBrand("Tesla");

        Mockito.when(vehicleRepository.findAll()).thenReturn(List.of(vehicle1));

        List<VehicleResponse> result = vehicleService.getAllVehicles(adminEmail, true, null);
       
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).hasSize(1);
        Assertions.assertThat(result.getFirst().brand()).isEqualTo("Tesla");
        Mockito.verify(vehicleRepository, Mockito.times(1)).findAll();
    }

    @Test
    public void VehicleService_GetAllVehiclesAsUser_ReturnsAllVehicles() {
        String userEmail = "user@test.invalid";
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setBrand("Mercedes-Benz");

        Mockito.when(vehicleRepository.findByOwnerEmail(userEmail)).thenReturn(List.of(vehicle1));

        List<VehicleResponse> result = vehicleService.getAllVehicles(userEmail, false, null);

        Assertions.assertThat(result).hasSize(1);
        Assertions.assertThat(result.getFirst().brand()).isEqualTo("Mercedes-Benz");
        Mockito.verify(vehicleRepository, Mockito.times(1)).findByOwnerEmail(userEmail);
    }

    @Test
    public void VehicleService_GetAllYourVehiclesAsUser_ReturnsAllYourVehicles() {
    
        String userEmail = "owner@test.invalid";
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setBrand("Nissan");
        Vehicle vehicle2 = new Vehicle();
        vehicle2.setBrand("BMW");

        Mockito.when(vehicleRepository.findByOwnerEmail(userEmail)).thenReturn(List.of(vehicle1, vehicle2));

        List<VehicleResponse> result = vehicleService.getAllVehicles(userEmail, false, null);

        Assertions.assertThat(result).hasSize(2);
        Mockito.verify(vehicleRepository, Mockito.times(1)).findByOwnerEmail(userEmail);
    }

    @Test
    public void VehicleService_GetAllSingleUsersVehiclesAsAdmin_ReturnsAllSingleUsersVehicles() {

        String adminEmail = "admin@test.invalid";
        String targetEmail = "target@test.invalid";
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setBrand("Audi");
        Vehicle vehicle2 = new Vehicle();
        vehicle1.setBrand("Ford");

        Mockito.when(vehicleRepository.findByOwnerEmail(targetEmail)).thenReturn(List.of(vehicle1, vehicle2));

        List<VehicleResponse> result = vehicleService.getAllVehicles(adminEmail, true, targetEmail);

        Assertions.assertThat(result).hasSize(2);
        Assertions.assertThat(result.get(0).brand()).isEqualTo("Audi");
        Assertions.assertThat(result.get(1).brand()).isEqualTo("Ford");
        Mockito.verify(vehicleRepository, Mockito.times(1)).findByOwnerEmail(targetEmail);
    }

    @Test
    public void VehicleService_GetAllSingleUsersVehiclesAsOtherUser_ReturnsException() {

        String userEmail = "user@test.invalid";
        String targetEmail = "otheruser@test.invalid";

        Assertions.assertThatThrownBy(() -> {
            vehicleService.getAllVehicles(userEmail, false, targetEmail);
        }).isInstanceOf(ForbiddenAccessException.class);
    }

}