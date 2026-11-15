package jz.pk.evcm.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Operator {

    @Id
    private Long id;
    private String name;
    private String websiteUrl;
    private String phone;
    private String email;
    private String faultReportEmail;

}
