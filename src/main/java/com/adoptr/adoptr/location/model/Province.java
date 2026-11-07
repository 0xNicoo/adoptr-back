package com.adoptr.adoptr.location.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Province {
    @Id
    private Long id;
    private String name;
}
