package jz.pk.evcm.controller;

import jz.pk.evcm.dto.res.ChargerPointResponse;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ChargePointController {

    @QueryMapping
    public ChargerPointResponse chargerPointById(@Argument Long id) {
        return null;
    }

}
