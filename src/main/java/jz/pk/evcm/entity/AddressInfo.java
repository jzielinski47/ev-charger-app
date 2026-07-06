package jz.pk.evcm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressInfo {

    @Id
    private Long id;
    private String title;
    private String addressLine1;
    private String addressLine2;
    private String town;
    private String stateOrProvince;
    private String postcode;
    private Double latitude;
    private Double longitude;

    @Column(columnDefinition = "TEXT")
    private String accessComments;

}