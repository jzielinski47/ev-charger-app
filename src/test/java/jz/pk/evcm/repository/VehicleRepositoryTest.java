package jz.pk.evcm.repository;

import jz.pk.evcm.entity.ConnectorType;
import jz.pk.evcm.entity.User;
import jz.pk.evcm.entity.Vehicle;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;
import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class VehicleRepositoryTest {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void VehicleRepository_save_ValidVehicle_ReturnsSavedVehicle() {
        // Arrange
        Vehicle vehicle = VehicleTestFactory.baseVehicleBuilder().build();

        // Act
        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        // Assert
        Assertions.assertThat(savedVehicle).isNotNull();
        Assertions.assertThat(savedVehicle.getId()).isGreaterThan(0);
        Assertions.assertThat(savedVehicle.getBrand()).isEqualTo("Tesla");
    }

    @Test
    public void VehicleRepository_save_WithNoConnectors_ReturnsSavedVehicle() {
        // Arrange
        Vehicle vehicle = VehicleTestFactory.baseVehicleBuilder().supportedConnectorTypes(List.of()).build();

        // Act
        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        // Assert
        Assertions.assertThat(savedVehicle).isNotNull();
        Assertions.assertThat(savedVehicle.getId()).isGreaterThan(0);
        Assertions.assertThat(savedVehicle.getBrand()).isEqualTo("Tesla");
        Assertions.assertThat(savedVehicle.getSupportedConnectorTypes().isEmpty());

    }

    @Test
    public void VehicleRepository_save_WithMultipleConnectors_ReturnsVehicleWithAllConnectors() {
        Vehicle vehicle = VehicleTestFactory.baseVehicleBuilder()
                .supportedConnectorTypes(List.of(
                        ConnectorType.TYPE_1,
                        ConnectorType.TYPE_2_TETHERED,
                        ConnectorType.TYPE_2_SOCKET_ONLY
                ))
                .build();

        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        Assertions.assertThat(savedVehicle.getSupportedConnectorTypes()).hasSize(3);
        Assertions.assertThat(savedVehicle.getSupportedConnectorTypes())
                .containsExactly(
                        ConnectorType.TYPE_1,
                        ConnectorType.TYPE_2_TETHERED,
                        ConnectorType.TYPE_2_SOCKET_ONLY
                );
    }

    @Test
    public void VehicleRepository_findAll_ReturnsAllSavedVehicles() {
        Vehicle vehicle1 = VehicleTestFactory.baseVehicleBuilder().model("Model Y").build();
        Vehicle vehicle2 = VehicleTestFactory.baseVehicleBuilder().model("Model 3").build();
        vehicleRepository.saveAll(List.of(vehicle1, vehicle2));

        List<Vehicle> foundVehicles = vehicleRepository.findAll();

        Assertions.assertThat(foundVehicles).hasSize(2);
        Assertions.assertThat(foundVehicles)
                .extracting(Vehicle::getModel)
                .containsExactlyInAnyOrder("Model Y", "Model 3");
    }

    @Test
    public void VehicleRepository_findById_ExistingId_ReturnsVehicle() {
        Vehicle vehicle = VehicleTestFactory.baseVehicleBuilder().build();
        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        Optional<Vehicle> foundVehicle = vehicleRepository.findById(savedVehicle.getId());

        Assertions.assertThat(foundVehicle).isPresent();
        Assertions.assertThat(foundVehicle.get().getId()).isEqualTo(savedVehicle.getId());
        Assertions.assertThat(foundVehicle.get().getBrand()).isEqualTo("Tesla");
    }

    @Test
    public void VehicleRepository_findById_NonExistingId_ReturnsEmptyOptional() {
        Optional<Vehicle> foundVehicle = vehicleRepository.findById(999L);

        Assertions.assertThat(foundVehicle).isEmpty();
    }

    @Test
    public void VehicleRepository_findByOwnerEmail_ExistingEmail_ReturnsListOfVehicles() {
        User owner = User.builder().email("user@email.test").name("name").surname("sur").password("1234").build();
        entityManager.persist(owner);

        Vehicle vehicle1 = VehicleTestFactory.baseVehicleBuilder().owner(owner).build();
        Vehicle vehicle2 = VehicleTestFactory.baseVehicleBuilder().owner(owner).model("Model 3").build();
        vehicleRepository.saveAll(List.of(vehicle1, vehicle2));

        List<Vehicle> foundVehicles = vehicleRepository.findByOwnerEmail("user@email.test");

        Assertions.assertThat(foundVehicles).hasSize(2);
        Assertions.assertThat(foundVehicles)
                .extracting(Vehicle::getModel)
                .containsExactlyInAnyOrder("Model Y", "Model 3");
    }

    @Test
    public void VehicleRepository_findByIdAndOwnerEmail_WrongEmail_ReturnsEmptyOptional() {
        User owner = User.builder().email("user@email.test").name("name").surname("sur").password("1234").build();
        entityManager.persist(owner);

        Vehicle vehicle = VehicleTestFactory.baseVehicleBuilder().owner(owner).build();
        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        Optional<Vehicle> foundVehicle = vehicleRepository.findByIdAndOwnerEmail(savedVehicle.getId(), "wrong@email.test");

        Assertions.assertThat(foundVehicle).isEmpty();
    }

    @Test
    public void VehicleRepository_delete_ExistingVehicle_RemovesVehicle() {
        Vehicle vehicle = VehicleTestFactory.baseVehicleBuilder().build();
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        Long id = savedVehicle.getId();

        vehicleRepository.delete(savedVehicle);

        Optional<Vehicle> deletedVehicle = vehicleRepository.findById(id);
        Assertions.assertThat(deletedVehicle).isEmpty();
    }

}
