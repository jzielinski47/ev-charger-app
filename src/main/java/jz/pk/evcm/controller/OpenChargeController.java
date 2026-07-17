package jz.pk.evcm.controller;

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

    OpenChargeApiService openChargerAPI;

    public OpenChargeController(OpenChargeApiService openChargerAPI) {
        this.openChargerAPI = openChargerAPI;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ChargerPointRequest>> fetchAllChargersInProximity() {
        List<ChargerPointRequest> req = openChargerAPI.fetchAllChargersInProximity(50.0,20.0,10.);

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
