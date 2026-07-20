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
import java.util.List;

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

    @Test
    public void VehicleRepository_SaveAll_WithSingleSupportedConnector_ReturnsSavedVehicle() {

        // Arrange
        Vehicle vehicle = Vehicle.builder()
                .brand("Mercedes")
                .model("EQS")
                .yearOfProduction(Year.of(2022))
                .supportedConnectorTypes(List.of(ConnectorType.TYPE_1))
                .build();

        // Act
        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        // Assert
        Assertions.assertThat(savedVehicle).isNotNull();
        Assertions.assertThat(savedVehicle.getId()).isGreaterThan(0);
        Assertions.assertThat(savedVehicle.getSupportedConnectorTypes().size()).isEqualTo(1);
        Assertions.assertThat(savedVehicle.getSupportedConnectorTypes().getFirst()).isEqualTo(ConnectorType.TYPE_1);

    }

    @Test
    public void VehicleRepository_SaveAll_WithMultipleSupportedConnectors_ReturnsSavedVehicle() {

        // Arrange
        Vehicle vehicle = Vehicle.builder()
                .brand("Mercedes")
                .model("EQS")
                .yearOfProduction(Year.of(2022))
                .supportedConnectorTypes(List.of(ConnectorType.TYPE_1, ConnectorType.TYPE_2_TETHERED, ConnectorType.TYPE_2_SOCKET_ONLY))
                .build();

        // Act
        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        // Assert
        Assertions.assertThat(savedVehicle).isNotNull();
        Assertions.assertThat(savedVehicle.getId()).isGreaterThan(0);
        Assertions.assertThat(savedVehicle.getSupportedConnectorTypes().size()).isEqualTo(3);
        Assertions.assertThat(savedVehicle.getSupportedConnectorTypes().getFirst()).isEqualTo(ConnectorType.TYPE_1);
        Assertions.assertThat(savedVehicle.getSupportedConnectorTypes().get(1)).isEqualTo(ConnectorType.TYPE_2_TETHERED);
        Assertions.assertThat(savedVehicle.getSupportedConnectorTypes().get(2)).isEqualTo(ConnectorType.TYPE_2_SOCKET_ONLY);

    }

}
