package jz.pk.evcm.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.Instant;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChargerPoint {

    @Id
    @Column(name = "charger_point_id")
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_info_id")
    private AddressInfo addressInfo;

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "chargerPoint", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Connection> connections;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "operator_id")
    private Operator operator;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "data_provider_id")
    private DataProvider dataProvider;

    private Boolean isOperational;

    @Column(columnDefinition = "TEXT")
    private String usageCost;

    private Integer numberOfPoints;
    private Instant dateLastVerified;
    private Instant dateLastConfirmed;

}
