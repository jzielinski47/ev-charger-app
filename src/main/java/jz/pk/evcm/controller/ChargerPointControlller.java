package jz.pk.evcm.controller;

import jz.pk.evcm.dto.req.ocm.ChargerPointDto;
import jz.pk.evcm.service.OCMService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ChargerPointControlller {

    OCMService openChargerAPI;

    public ChargerPointControlller(OCMService openChargerAPI) {
        this.openChargerAPI = openChargerAPI;
    }

    @GetMapping("/")
    public ResponseEntity<List<ChargerPointDto>> test() {
        List<ChargerPointDto> req = openChargerAPI.ocmTestEndpoint();
        System.out.println(req);
        return new ResponseEntity<>(req, HttpStatus.FOUND);

    }

    @GetMapping("/test")
    public ResponseEntity<String> test1() {
        String req = String.valueOf(openChargerAPI.test());
        System.out.println(req);
        return new ResponseEntity<>(req, HttpStatus.FOUND);

    }

}
