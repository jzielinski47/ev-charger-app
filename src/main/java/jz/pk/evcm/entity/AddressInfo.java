package jz.pk.evcm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

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

    /*
    * The Point type from JTS (Java Topology Suite) represents geographic coordinates,
    * and the 4326 SRID indicates the WGS84 coordinate system (standard for GPS coordinates)
    * */
    @Column(columnDefinition = "geometry(Point,4326)")
    private Point location;

    @Column(columnDefinition = "TEXT")
    private String accessComments;

}