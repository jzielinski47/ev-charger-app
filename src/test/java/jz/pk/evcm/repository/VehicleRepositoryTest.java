package jz.pk.evcm.repository;

import jz.pk.evcm.entity.User;
import jz.pk.evcm.entity.Vehicle;
import jz.pk.evcm.testdata.TestContainersConfig;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.Year;
import java.util.List;

import static jz.pk.evcm.entity.ConnectorType.*;
import static jz.pk.evcm.testdata.TestVehicleDefaults.aVehicle;
import static jz.pk.evcm.testdata.TestVehicleDefaults.aVehicleOwnedBy;
import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestContainersConfig.class)
public class VehicleRepositoryTest {

    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private TestEntityManager em;

    @Autowired
    private DataSource ds;

    private User persistUser(String email) {
        return em.persist(User.builder().email(email).name("name").surname("sur").password("1234").build());
    }

    @Nested
    class Mapping {

        @Test
        void persistsAllFieldsAndConnectors() throws SQLException {
            Vehicle vehicle = aVehicle().supportedConnectorTypes(List.of(TYPE_1, TYPE_2_TETHERED, CCS2)).build();

            Vehicle insertedVehicle = vehicleRepository.save(vehicle);
            em.flush();
            em.clear();

            Vehicle reloadedVehicle = vehicleRepository.findById(insertedVehicle.getId()).orElseThrow();

            assertThat(reloadedVehicle).isNotSameAs(vehicle);
            assertThat(reloadedVehicle.getBrand()).isEqualTo("Tesla");
            assertThat(reloadedVehicle.getModel()).isEqualTo("Model Y");
            assertThat(reloadedVehicle.getYearOfProduction()).isEqualTo(Year.of(2026));
            assertThat(reloadedVehicle.getSupportedConnectorTypes())
                    .containsExactlyInAnyOrder(TYPE_1, TYPE_2_TETHERED, CCS2);
        }
    }

    @Nested
    class FindByOwnerEmail {

        @Test
        void returnsOnlyVehiclesOfGivenOwner() {
            User john = persistUser("john@test.dev");
            User anna = persistUser("anna@test.dev");
            em.persist(aVehicleOwnedBy(john).model("Model Y").build());
            em.persist(aVehicleOwnedBy(john).model("Model 3").build());
            em.persist(aVehicleOwnedBy(anna).model("Model S").build());

            em.flush();
            em.clear();

            List<Vehicle> result = vehicleRepository.findByOwnerEmail("john@test.dev");

            assertThat(result).extracting(Vehicle::getModel)
                    .containsExactlyInAnyOrder("Model Y", "Model 3");
        }

        @Test
        void returnsEmptyList_whenOwnerHasNoVehicles() {
            persistUser("john@test.dev");
            assertThat(vehicleRepository.findByOwnerEmail("john@test.dev")).isEmpty();
        }
    }

    @Nested
    class FindByIdAndOwnerEmail {

        @Test
        void returnsVehicle_whenOwnerMatches() {
            User john = persistUser("john@test.dev");
            Vehicle saved = em.persistFlushFind(aVehicleOwnedBy(john).build());

            assertThat(vehicleRepository.findByIdAndOwnerEmail(saved.getId(), "john@test.dev"))
                    .isPresent();
        }

        @Test
        void returnsEmpty_whenOwnerDiffers() {
            User john = persistUser("john@test.dev");
            persistUser("anna@test.dev");
            Vehicle saved = em.persistFlushFind(aVehicleOwnedBy(john).build());

            assertThat(vehicleRepository.findByIdAndOwnerEmail(saved.getId(), "anna@test.dev"))
                    .isEmpty();
        }
    }

}
