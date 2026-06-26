package jz.pk.evcm.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ChargerPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;
}
