package jz.pk.evcm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Connection {

    @Id
    private Long id;
    private Integer amps;
    private Integer voltage;
    private Double powerKW;
    private Integer currentTypeId;
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    private ConnectorType connectorType;

    @Enumerated(EnumType.STRING)
    private CurrentType currentType;




}