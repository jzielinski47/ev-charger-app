package jz.pk.evcm.controller;

import jz.pk.evcm.dto.req.ocm.ChargerPointDto;
import jz.pk.evcm.service.OpenChargeApiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chargers")
public class ChargerPointController {

    OpenChargeApiService openChargerAPI;

    public ChargerPointController(OpenChargeApiService openChargerAPI) {
        this.openChargerAPI = openChargerAPI;
    }

    /*
    * USER ENDPOINT (PLANNED FOR GRAPH QL)
    * */



    /*
    * EXTERNAL API REQUESTS
    * ADMIN ONLY ENDPOINTS
    * */

    @GetMapping("/ocm/poi")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ChargerPointDto>> fetchAllChargersInProximity() {
        List<ChargerPointDto> req = openChargerAPI.fetchAllChargersInProximity(50.0,20.0,10.);

        if (req == null) {
            System.out.println("OpenCharge API returned NULL!");
            return ResponseEntity.internalServerError().build();
        }

        System.out.println("Fetched " + req.size() + " chargers from OpenCharge API");
        return new ResponseEntity<>(req, HttpStatus.OK);

    }

    @PostMapping("/ocm/save")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ChargerPointDto>> fetchAllChargersInProximityAndSave(@RequestParam Double latitude, @RequestParam Double longitude, @RequestParam Double distance) {
        System.out.println("Trying fetching all chargers");

        List<ChargerPointDto> req = openChargerAPI.fetchChargersAndSave(latitude,longitude, distance, 100);

        if (req == null) {
            System.out.println("OpenCharge API returned NULL!");
            return ResponseEntity.internalServerError().build();
        }

        System.out.println("Fetched " + req.size() + " chargers from OpenCharge API");
        return new ResponseEntity<>(req, HttpStatus.OK);

    }

}
