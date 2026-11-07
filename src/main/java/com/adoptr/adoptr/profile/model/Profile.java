package com.adoptr.adoptr.profile.model;

import com.adoptr.adoptr.location.model.Locality;
import com.adoptr.adoptr.user.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "profile_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
public abstract class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "locality_id")
    private Locality locality;

    public abstract ProfileType getProfileType();
}
