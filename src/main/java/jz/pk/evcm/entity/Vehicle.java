package jz.pk.evcm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Year;
import java.util.Set;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User owner;

    private String brand;
    private String model;
    private Year yearOfProduction;

    @Enumerated(EnumType.STRING)
    private VehicleOriginatingMarket vehicleOriginatingMarket;

    @ElementCollection(targetClass = VehicleInlet.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "vehicle_inlets", joinColumns = @JoinColumn(name = "vehicle_id"))
    @Column(name = "inlet")
    private Set<VehicleInlet> supportedInlets;

    private Double batteryCapacityKwh;
    private Double maxAcPowerKw;
    private Double maxDcPowerKw;
    private Integer acPhases;

}
