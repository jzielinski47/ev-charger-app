package jz.pk.evcm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Connection {

    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "charger_point_id")
    private ChargerPoint chargerPoint;

    private Integer amps;
    private Integer voltage;
    private Double powerKW;
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    private ConnectorType connectorType;

    @Enumerated(EnumType.STRING)
    private CurrentType currentType;


}