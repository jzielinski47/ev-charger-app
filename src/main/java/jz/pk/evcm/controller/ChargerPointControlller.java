package jz.pk.evcm.controller;

import jz.pk.evcm.service.OpenChargeApiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chargers")
public class ChargerPointControlller {

    OpenChargeApiService openChargerAPI;

    public ChargerPointControlller(OpenChargeApiService openChargerAPI) {
        this.openChargerAPI = openChargerAPI;
    }

    @GetMapping("/ocm")
    public ResponseEntity<String> test1() {
        String req = String.valueOf(openChargerAPI.fetchAllChargersInProximity(50.0,20.0,10.));
        System.out.println(req);
        return new ResponseEntity<>(req, HttpStatus.FOUND);

    }

}
