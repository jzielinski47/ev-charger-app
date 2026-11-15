package jz.pk.evcm.controller.app;

import jz.pk.evcm.dto.req.local.ChargerFilter;
import jz.pk.evcm.dto.req.local.ChargerSearchOptions;
import jz.pk.evcm.dto.req.local.ChargerSort;
import jz.pk.evcm.dto.res.ChargerPointResponse;
import jz.pk.evcm.service.LocalChargePointService;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@AllArgsConstructor
public class ChargePointController {

    private final LocalChargePointService chargePointService;

    @QueryMapping
    public ChargerPointResponse chargerPointById(@Argument Long id) {
        return chargePointService.getChargerPointById(id);
    }

    @QueryMapping
    public List<ChargerPointResponse> allChargerPointsInProximity(@Argument Double lat, @Argument Double lon,
                                                                  @Argument Double proximity, @Argument Long vehicleId,
                                                                  @Argument ChargerFilter filter, @Argument ChargerSort sort,
                                                                  @Argument Integer limit, @Argument Integer offset,
                                                                  @AuthenticationPrincipal UserDetails principal) {
        String ownerEmail = principal != null ? principal.getUsername() : null;
        return chargePointService.getAllChargersInProximity(lat, lon, proximity, vehicleId, ownerEmail,
                new ChargerSearchOptions(filter, sort, limit, offset));
    }

    @QueryMapping
    public List<ChargerPointResponse> allChargerPointsInBounds(@Argument Double north, @Argument Double south,
                                                               @Argument Double east, @Argument Double west,
                                                               @Argument Long vehicleId,
                                                               @Argument ChargerFilter filter, @Argument ChargerSort sort,
                                                               @Argument Integer limit, @Argument Integer offset,
                                                               @AuthenticationPrincipal UserDetails principal) {
        String ownerEmail = principal != null ? principal.getUsername() : null;
        return chargePointService.getAllChargersInBounds(north, south, east, west, vehicleId, ownerEmail,
                new ChargerSearchOptions(filter, sort, limit, offset));
    }

}
