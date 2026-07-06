package jz.pk.evcm.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionType {
    @Id
    private Long id;
    private String title;
    private String formalName;
    private Boolean isDiscontinued; // If true, this is an discontinued but used connection type
    private Boolean isObsolete; // If true, this is an obsolete connection type and is unlikely to be present in modern infrastructure


}