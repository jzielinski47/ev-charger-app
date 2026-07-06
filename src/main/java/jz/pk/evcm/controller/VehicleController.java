package jz.pk.evcm.controller;

import jz.pk.evcm.dto.req.local.InputVehicleDto;
import jz.pk.evcm.dto.res.ResVehicleDto;
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
    public List<ResVehicleDto> getVehicles(Authentication auth, @RequestParam(required = false) String targetEmail) {
        return vehicleService.getAllVehicles(auth.getName(), isAdmin(auth), targetEmail);
    }

    @GetMapping("/{id}")
    public ResVehicleDto getVehicleById(Authentication auth, @PathVariable Long id) {
        return vehicleService.getVehicleById(id, auth.getName(), isAdmin(auth));
    }

    @PostMapping
    public ResVehicleDto addVehicle(Authentication auth, @RequestBody InputVehicleDto dto) {
        return vehicleService.addVehicle(dto, auth.getName(), isAdmin(auth));
    }

    @PutMapping("/{id}")
    public ResVehicleDto modifyVehicle(
            Authentication auth,
            @PathVariable Long id,
            @RequestBody InputVehicleDto dto) {
        return vehicleService.modifyVehicle(id, dto, auth.getName(), isAdmin(auth));
    }

    @DeleteMapping("/{id}")
    public void removeVehicle(Authentication auth, @PathVariable Long id) {
        vehicleService.removeVehicle(id, auth.getName(), isAdmin(auth));
    }

    @PostMapping("/{id}/favourite")
    public boolean setFavouriteVehicle(Authentication auth, @PathVariable Long id) {
        return vehicleService.setFavouriteVehicle(id, auth.getName(), isAdmin(auth));
    }
}