package jz.pk.evcm.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class VehicleInletMigrationTest {

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer(
            DockerImageName.parse("postgis/postgis:16-3.5-alpine").asCompatibleSubstituteFor("postgres"));

    private static long mixedType2AndTeslaVehicleId;
    private static long nativeInletsVehicleId;
    private static long unknownOnlyVehicleId;

    @BeforeAll
    static void migrateLegacyData() throws SQLException {
        flyway("2").migrate();

        try (Connection connection = connect()) {
            mixedType2AndTeslaVehicleId = insertVehicle(connection,
                    List.of("TYPE_2_TETHERED", "TYPE_2", "TESLA", "UNKNOWN", "CHADEMO"));
            nativeInletsVehicleId = insertVehicle(connection,
                    List.of("TYPE_2_SOCKET_ONLY", "CCS2", "TYPE_1", "NACS"));
            unknownOnlyVehicleId = insertVehicle(connection, List.of("UNKNOWN"));
        }

        flyway("latest").migrate();
    }

    @Test
    void mapsLegacyConnectorTypesToInlets_andDropsUnknown() throws SQLException {
        assertThat(inletsOf(mixedType2AndTeslaVehicleId)).containsExactlyInAnyOrder("TYPE_2", "NACS", "CHADEMO");
        assertThat(inletsOf(nativeInletsVehicleId)).containsExactlyInAnyOrder("TYPE_2", "CCS2", "TYPE_1", "NACS");
        assertThat(inletsOf(unknownOnlyVehicleId)).isEmpty();
    }

    @Test
    void dropsLegacyConnectorTypesTable() throws SQLException {
        try (Connection connection = connect();
             ResultSet tables = connection.getMetaData().getTables(null, "public", "vehicle_connector_types", null)) {
            assertThat(tables.next()).isFalse();
        }
    }

    private static Flyway flyway(String target) {
        return Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db/migration")
                .target(target)
                .load();
    }

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
    }

    private static long insertVehicle(Connection connection, List<String> connectorTypes) throws SQLException {
        long vehicleId;
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO vehicle (brand) VALUES ('Legacy') RETURNING id");
             ResultSet keys = insert.executeQuery()) {
            keys.next();
            vehicleId = keys.getLong(1);
        }

        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO vehicle_connector_types (vehicle_id, connector_type) VALUES (?, ?)")) {
            for (String connectorType : connectorTypes) {
                insert.setLong(1, vehicleId);
                insert.setString(2, connectorType);
                insert.executeUpdate();
            }
        }
        return vehicleId;
    }

    private static Set<String> inletsOf(long vehicleId) throws SQLException {
        Set<String> inlets = new HashSet<>();
        try (Connection connection = connect();
             PreparedStatement select = connection.prepareStatement(
                     "SELECT inlet FROM vehicle_inlets WHERE vehicle_id = ?")) {
            select.setLong(1, vehicleId);
            try (ResultSet rows = select.executeQuery()) {
                while (rows.next()) {
                    inlets.add(rows.getString(1));
                }
            }
        }
        return inlets;
    }
}
