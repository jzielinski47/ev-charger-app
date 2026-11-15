package jz.pk.evcm.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "car_brand")
public class CarBrand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // slug is url-safe id for a brand eg. mercedes-benz
    @Column(nullable = false, unique = true, length = 64)
    private String slug;

    @Column(nullable = false, length = 100)
    private String name;
}
