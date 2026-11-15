package jz.pk.evcm.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.util.Set;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "car_model")
public class CarModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "brand_id")
    private CarBrand brand;

    @Column(nullable = false, length = 100)
    private String name;

    @BatchSize(size = 100)
    @ElementCollection(targetClass = VehicleInlet.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "car_model_inlets", joinColumns = @JoinColumn(name = "model_id"))
    @Column(name = "inlet")
    private Set<VehicleInlet> supportedInlets;

    private Double batteryCapacityKwh;
    private Double maxAcPowerKw;
    private Double maxDcPowerKw;
    private Integer acPhases;
}
