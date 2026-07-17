package jz.pk.evcm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Year;
import java.util.List;

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
    private VehicleOriginatingMarket vehicleOriginatingMarket;

    @ElementCollection(targetClass = ConnectorType.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "vehicle_connector_types", joinColumns = @JoinColumn(name = "vehicle_id"))
    @Column(name = "connector_type")
    private List<ConnectorType> supportedConnectorTypes;

    private Double batteryCapacityKwh;
    private Double maxAcPowerKw;
    private Double maxDcPowerKw;
    private Integer acPhases;

}
