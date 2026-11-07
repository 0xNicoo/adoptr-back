package com.adoptr.adoptr.profile.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "person_profile")
@DiscriminatorValue("PERSON")
@Getter
@Setter
public class PersonProfile extends Profile {
    private String firstName;
    private String lastName;

    @Enumerated(EnumType.STRING)
    private Gender genderType;

    @Override
    public ProfileType getProfileType() {
        return ProfileType.PERSON;
    }
}
