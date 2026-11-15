package jz.pk.evcm.controller.integration;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jz.pk.evcm.dto.req.ocm.ChargerPointRequest;
import jz.pk.evcm.service.OpenChargeApiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/chargers")
public class OpenChargeController {

    private static final int MAX_RESULTS = 5000;

    OpenChargeApiService openChargerAPI;

    public OpenChargeController(OpenChargeApiService openChargerAPI) {
        this.openChargerAPI = openChargerAPI;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ChargerPointRequest>> fetchAllChargersInProximity(
            @RequestParam @DecimalMin("-90") @DecimalMax("90") Double latitude,
            @RequestParam @DecimalMin("-180") @DecimalMax("180") Double longitude,
            @RequestParam @Positive Double distance,
            @RequestParam(defaultValue = "100") @Min(1) @Max(MAX_RESULTS) Integer maxResults) {
        List<ChargerPointRequest> req = openChargerAPI.fetchAllChargersInProximity(latitude, longitude, distance,
                maxResults);

        if (req == null) {
            System.out.println("OpenCharge API returned NULL!");
            return ResponseEntity.internalServerError().build();
        }

        System.out.println("Fetched " + req.size() + " chargers from OpenCharge API");
        return new ResponseEntity<>(req, HttpStatus.OK);

    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ChargerPointRequest>> fetchAllChargersInProximityAndSave(@RequestParam Double latitude, @RequestParam Double longitude, @RequestParam Double distance) {
        System.out.println("Trying fetching all chargers");

        List<ChargerPointRequest> req = openChargerAPI.fetchChargersAndSave(latitude, longitude, distance, 100);

        if (req == null) {
            System.out.println("OpenCharge API returned NULL!");
            return ResponseEntity.internalServerError().build();
        }

        System.out.println("Fetched " + req.size() + " chargers from OpenCharge API");
        return new ResponseEntity<>(req, HttpStatus.OK);
    }

}
