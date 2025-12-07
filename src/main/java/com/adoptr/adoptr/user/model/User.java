package com.adoptr.adoptr.user.model;

import com.adoptr.adoptr.profile.model.Profile;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String provider;
    private String providerUserId;
    private String email;
    private String name;
    private LocalDateTime createdAt;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Profile profile;

    @PrePersist
    public void prePersist(){
        this.createdAt = LocalDateTime.now();
    }

}
