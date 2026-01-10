package com.adoptr.adoptr.publication.model;

import com.adoptr.adoptr.location.model.Locality;
import com.adoptr.adoptr.user.model.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Inheritance(strategy = InheritanceType.JOINED)
public class Publication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String title;

    @Enumerated(EnumType.STRING)
    private PublicationType type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private LocalDateTime creationDate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    private User user;


    @Column
    private Long imageId;

    @ManyToOne
    @JoinColumn(name = "locality_id")
    private Locality locality;

}
