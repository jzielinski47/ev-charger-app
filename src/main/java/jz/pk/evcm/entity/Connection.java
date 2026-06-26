package jz.pk.evcm.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Connection {

    @Id
    private Long id;
    private Integer amps;
    private Integer voltage;
    private Double powerKW;
    private Integer currentTypeId;
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "connection_type_id")
    private ConnectionType connectionType;


}