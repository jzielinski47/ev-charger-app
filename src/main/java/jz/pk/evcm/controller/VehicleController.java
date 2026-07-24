package jz.pk.evcm.controller;

import jz.pk.evcm.dto.req.local.VehicleRequest;
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

    @GetMapping
    public List<VehicleResponse> getVehicles(Authentication auth, @RequestParam(required = false) String targetEmail) {
        return vehicleService.getAllVehicles(auth.getName(), hasAdminAuthority(auth), targetEmail);
    }

    @GetMapping("/{vehicleId}")
    public VehicleResponse getVehicleById(Authentication auth, @PathVariable Long vehicleId) {
        return vehicleService.getVehicleById(vehicleId, auth.getName(), hasAdminAuthority(auth));
    }

    @PostMapping
    public VehicleResponse addVehicle(
            Authentication auth,
            @RequestBody VehicleRequest dto,
            @RequestParam(required = false) String targetEmail) {
        return vehicleService.addVehicle(dto, auth.getName(), hasAdminAuthority(auth), targetEmail);
    }

    @PutMapping("/{vehicleId}")
    public VehicleResponse modifyVehicle(
            Authentication auth,
            @PathVariable Long vehicleId,
            @RequestBody VehicleRequest dto) {
        return vehicleService.modifyVehicle(vehicleId, dto, auth.getName(), hasAdminAuthority(auth));
    }

    @DeleteMapping("/{vehicleId}")
    public void deleteVehicle(Authentication auth, @PathVariable Long vehicleId) {
        vehicleService.deleteVehicle(vehicleId, auth.getName(), hasAdminAuthority(auth));
    }

    private boolean hasAdminAuthority(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));
    }
}
