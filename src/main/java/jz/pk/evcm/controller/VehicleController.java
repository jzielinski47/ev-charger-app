package jz.pk.evcm.controller;

import jz.pk.evcm.dto.res.VehicleResponse;
import jz.pk.evcm.service.VehicleService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));
    }

    @GetMapping
    public List<VehicleResponse> getVehicles(Authentication auth, @RequestParam(required = false) String targetEmail) {
        return vehicleService.getAllVehicles(auth.getName(), isAdmin(auth), targetEmail);
    }



}