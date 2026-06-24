package jz.pk.evcm.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String surname;
    private String email;
    private String password;
    private UserRole role;

    @OneToMany(mappedBy = "owner")
    private Set<Vehicle> userVehicles = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_vehicle_id")
    private Vehicle selectedVehicle;

    public Optional<Vehicle> getSelectedVehicle() {
        return Optional.ofNullable(selectedVehicle);
    }
}
