package com.adoptr.adoptr.profile.model;

import com.adoptr.adoptr.location.model.Locality;
import com.adoptr.adoptr.user.model.User;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;

    @Enumerated(EnumType.STRING)
    private Gender genderType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToOne
    private User user;

    @ManyToOne
    @JoinColumn(name = "locality_id")
    private Locality locality;
}
