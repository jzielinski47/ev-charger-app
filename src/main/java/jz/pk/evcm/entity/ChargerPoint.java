package jz.pk.evcm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChargerPoint {

    @Id
    @Column(name = "charger_point_id")
    private Long chargerPointId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_info_id")
    private AddressInfo addressInfo;

    @OneToMany(mappedBy = "chargerPoint", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Connection> connections;

}