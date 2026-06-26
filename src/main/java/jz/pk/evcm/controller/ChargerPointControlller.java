package jz.pk.evcm.controller;

import jz.pk.evcm.dto.req.ocm.ChargerPointDto;
import jz.pk.evcm.service.OpenChargeApiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chargers")
public class ChargerPointControlller {

    OpenChargeApiService openChargerAPI;

    public ChargerPointControlller(OpenChargeApiService openChargerAPI) {
        this.openChargerAPI = openChargerAPI;
    }

    @GetMapping("/poi")
    public ResponseEntity<List<ChargerPointDto>> fetchAllChargersInProximity() {
        List<ChargerPointDto> req = openChargerAPI.fetchAllChargersInProximity(50.0,20.0,10.);
        System.out.println("Fetched " + req.size() + " chargers from OpenCharge API");
        return new ResponseEntity<>(req, HttpStatus.FOUND);

    }

    @GetMapping("/save")
    public ResponseEntity<List<ChargerPointDto>> fetchAllChargersInProximityAndSave() {
        List<ChargerPointDto> req = openChargerAPI.fetchChargersAndSave(50.0,20.0,10., 100);
        System.out.println("Fetched " + req.size() + " chargers from OpenCharge API");
        return new ResponseEntity<>(req, HttpStatus.FOUND);

    }

}
