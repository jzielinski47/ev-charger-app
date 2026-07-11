package jz.pk.evcm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Year;

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

    private ConnectorType connector;
    private boolean isConnectorModified;

    private Double maxAcPowerKw;
    private Double maxDcPowerKw;
    private Double batterYCapacityKwh;


}
