package jz.pk.evcm.service;

import jz.pk.evcm.dto.res.VehicleResponse;
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

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleService vehicleService;

    /*
    * Unit tests for service layer
    * tested method List<VehicleResponse> getAllVehicles(String userEmail, boolean isAdmin, String targetUserEmail)
    */

    @Test
    public void VehicleService_GetAllVehicles_AsAdmin_ReturnsAllVehicles() {

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
    public void VehicleService_GetAllVehicles_AsUser_ReturnsAllVehiclesOwnedByYou() {
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
    public void VehicleService_GetAllVehicles_OwnedByYou_AsUser_ReturnsAllVehiclesOwnedByYou() {

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
    public void VehicleService_GetAllVehicles_OwnedByUser_AsUser_AsAdmin_ReturnsAllVehicles() {

        String adminEmail = "admin@test.invalid";
        String targetEmail = "target@test.invalid";
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setBrand("Audi");
        Vehicle vehicle2 = new Vehicle();
        vehicle2.setBrand("Ford");

        Mockito.when(vehicleRepository.findByOwnerEmail(targetEmail)).thenReturn(List.of(vehicle1, vehicle2));

        List<VehicleResponse> result = vehicleService.getAllVehicles(adminEmail, true, targetEmail);

        Assertions.assertThat(result).hasSize(2);
        Assertions.assertThat(result)
                .extracting(VehicleResponse::brand)
                .containsExactlyInAnyOrderElementsOf(List.of("Audi", "Ford"));
        Mockito.verify(vehicleRepository, Mockito.times(1)).findByOwnerEmail(targetEmail);
    }

    @Test
    public void VehicleService_GetAllVehicles_OwnedByUser_AsOtherUser_ReturnsException() {

        String userEmail = "user@test.invalid";
        String targetEmail = "otheruser@test.invalid";

        Assertions.assertThatThrownBy(() -> {
            vehicleService.getAllVehicles(userEmail, false, targetEmail);
        }).isInstanceOf(ForbiddenAccessException.class);
    }

    /*
     * tested method VehicleResponse getVehicleById(Long vehicleId, String currentUserEmail, boolean isAdmin)
     */

    @Test
    public void VehilceService_GetVehicleById_AsAdmin_ReturnsVehicle() {}

    @Test
    public void VehilceService_GetVehicleById_AsAdmin_WithInvalidId_ReturnsException() {}

    @Test
    public void VehilceService_GetVehicleById_AsUser_OnOwnedVehicle_ReturnsVehicle() {}

    @Test
    public void VehilceService_GetVehicleById_AsUser_OnOthersVehicle_ReturnsException() {}

    @Test
    public void VehilceService_GetVehicleById_AsUser_WithInvalidId_ReturnsException() {}

}