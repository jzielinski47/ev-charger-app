package jz.pk.evcm.service;

import jz.pk.evcm.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleService vehicleService;

    @Test
    public void VehicleService_GetAllVehiclesAsAdmin_ReturnsAllVehicles() {

    }

    @Test
    public void VehicleService_GetAllVehiclesAsUser_ReturnsAllVehicles() {

    }

    @Test
    public void VehicleService_GetAllYourVehiclesAsUser_ReturnsAllYourVehicles() {

    }

    @Test
    public void VehicleService_GetAllSingleUsersVehiclesAsAdmin_ReturnsAllSingleUsersVehicles() {

    }

    @Test
    public void VehicleService_GetAllSingleUsersVehiclesAsOtherUser_ReturnsException() {

    }



}