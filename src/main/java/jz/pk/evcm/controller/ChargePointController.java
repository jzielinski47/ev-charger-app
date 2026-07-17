package jz.pk.evcm.controller;

import jz.pk.evcm.dto.res.ChargerPointResponse;
import jz.pk.evcm.service.LocalChargePointService;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@AllArgsConstructor
public class ChargePointController {

    private final LocalChargePointService chargePointService;

    @QueryMapping
    public ChargerPointResponse chargerPointById(@Argument Long id) {
        return null;
    }

    @QueryMapping
    public List<ChargerPointResponse> allChargerPointsInProximity(@Argument Double lat, @Argument Double lon, @Argument Double proximity) {
        return chargePointService.getAllChargersInProximity(lat, lon, proximity);
    }

}
