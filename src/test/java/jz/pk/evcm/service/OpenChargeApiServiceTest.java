package jz.pk.evcm.service;

import com.sun.net.httpserver.HttpServer;
import jz.pk.evcm.entity.ChargerPoint;
import jz.pk.evcm.entity.Operator;
import jz.pk.evcm.repository.ChargerPointRepository;
import jz.pk.evcm.testdata.TestContainersConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("it")
@Import(TestContainersConfig.class)
class OpenChargeApiServiceTest {

    private static final String OCM_RESPONSE = """
            [
              {
                "id": 101,
                "usageCost": "1,99 PLN/kWh",
                "numberOfPoints": 2,
                "dateLastVerified": "2024-05-10T12:30:00Z",
                "dateLastConfirmed": "2023-01-02T08:00:00Z",
                "statusType": {"id": 50, "title": "Operational", "isOperational": true, "isUserSelectable": true},
                "operatorInfo": {
                  "id": 7,
                  "title": "GreenWay",
                  "websiteURL": "https://greenway.example",
                  "phonePrimaryContact": "+48 123 456 789",
                  "phoneSecondaryContact": "+48 000 000 000",
                  "contactEmail": "contact@greenway.example",
                  "faultReportEmail": "faults@greenway.example",
                  "isRestrictedEdit": false
                },
                "dataProvider": {"id": 1, "title": "Open Charge Map Contributors", "license": "Licensed under Creative Commons Attribution 4.0 International (CC BY 4.0)", "isOpenDataLicensed": true},
                "addressInfo": {"id": 101, "title": "Rynek", "latitude": 50.06, "longitude": 19.93},
                "connections": []
              },
              {
                "id": 102,
                "statusType": {"id": 100, "title": "Not Operational", "isOperational": false},
                "operatorInfo": {
                  "id": 7,
                  "title": "GreenWay",
                  "websiteURL": "https://greenway.example",
                  "phonePrimaryContact": "+48 123 456 789",
                  "contactEmail": "contact@greenway.example",
                  "faultReportEmail": "faults@greenway.example"
                },
                "dataProvider": {"id": 1, "title": "Open Charge Map Contributors", "license": "Licensed under Creative Commons Attribution 4.0 International (CC BY 4.0)", "isOpenDataLicensed": true},
                "addressInfo": {"id": 102, "latitude": 50.07, "longitude": 19.94},
                "connections": []
              },
              {
                "id": 103,
                "addressInfo": {"id": 103, "latitude": 50.08, "longitude": 19.95}
              }
            ]
            """;

    private static final HttpServer OCM = startFakeOcm();

    @Autowired
    private OpenChargeApiService service;

    @Autowired
    private ChargerPointRepository chargerPointRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @DynamicPropertySource
    static void ocmBaseUrl(DynamicPropertyRegistry registry) {
        registry.add("opencharge.api.base-url", () -> "http://localhost:" + OCM.getAddress().getPort());
    }

    private static HttpServer startFakeOcm() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.createContext("/poi", exchange -> {
                byte[] body = OCM_RESPONSE.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length);
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(body);
                }
            });
            server.start();
            return server;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @AfterAll
    static void stopFakeOcm() {
        OCM.stop(0);
    }

    @AfterEach
    void cleanUp() {
        chargerPointRepository.deleteAll();
    }

    @Test
    void fetchChargersAndSave_persistsOcmDetailsAndSharesOperatorBetweenChargers() {
        service.fetchChargersAndSave(50.06, 19.93, 10.0, 10);

        transactionTemplate.executeWithoutResult(status -> {
            ChargerPoint full = chargerPointRepository.findById(101L).orElseThrow();
            assertThat(full.getUsageCost()).isEqualTo("1,99 PLN/kWh");
            assertThat(full.getNumberOfPoints()).isEqualTo(2);
            assertThat(full.getDateLastVerified()).isEqualTo(Instant.parse("2024-05-10T12:30:00Z"));
            assertThat(full.getDateLastConfirmed()).isEqualTo(Instant.parse("2023-01-02T08:00:00Z"));
            assertThat(full.getIsOperational()).isTrue();

            Operator operator = full.getOperator();
            assertThat(operator.getId()).isEqualTo(7L);
            assertThat(operator.getName()).isEqualTo("GreenWay");
            assertThat(operator.getWebsiteUrl()).isEqualTo("https://greenway.example");
            assertThat(operator.getPhone()).isEqualTo("+48 123 456 789");
            assertThat(operator.getEmail()).isEqualTo("contact@greenway.example");
            assertThat(operator.getFaultReportEmail()).isEqualTo("faults@greenway.example");

            assertThat(full.getDataProvider().getId()).isEqualTo(1L);
            assertThat(full.getDataProvider().getLicense())
                    .isEqualTo("Licensed under Creative Commons Attribution 4.0 International (CC BY 4.0)");

            ChargerPoint notOperational = chargerPointRepository.findById(102L).orElseThrow();
            assertThat(notOperational.getIsOperational()).isFalse();
            assertThat(notOperational.getOperator().getId()).isEqualTo(7L);
            assertThat(notOperational.getDataProvider().getId()).isEqualTo(1L);

            ChargerPoint withoutDetails = chargerPointRepository.findById(103L).orElseThrow();
            assertThat(withoutDetails.getOperator()).isNull();
            assertThat(withoutDetails.getDataProvider()).isNull();
            assertThat(withoutDetails.getIsOperational()).isNull();
        });
    }

}
