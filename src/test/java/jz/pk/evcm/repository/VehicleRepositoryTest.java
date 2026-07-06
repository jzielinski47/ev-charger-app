package jz.pk.evcm.repository;

import jz.pk.evcm.entity.ConnectorType;
import jz.pk.evcm.entity.Vehicle;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.Year;
import java.util.Date;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class VehicleRepositoryTest {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Test
    public void VehicleRepository_SaveAll_ReturnsSavedVehicle() {

        // Arrange
        Vehicle vehicle = Vehicle.builder()
                .brand("Mercedes")
                .model("EQS")
                .yearOfProduction(Year.of(2022))
                .build();

        // Act
        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        // Assert
        Assertions.assertThat(savedVehicle).isNotNull();
        Assertions.assertThat(savedVehicle.getId()).isGreaterThan(0);

    }

}
