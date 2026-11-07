package com.adoptr.adoptr.location.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(indexes = @Index(name = "idx_locality_province", columnList = "province_id"))
public class Locality {
    @Id
    private Long id;
    private String name;

    /** Nombre para mostrar: agrega el departamento solo si el nombre se repite en la provincia. */
    private String displayName;

    /** Nombre en minúsculas y sin tildes,para búsqueda. */
    private String searchName;

    private String departmentName;

    @ManyToOne
    @JoinColumn(name = "province_id")
    private Province province;
}
