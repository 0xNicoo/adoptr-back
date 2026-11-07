package com.adoptr.adoptr.profile.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "organization_profile")
@DiscriminatorValue("ORGANIZATION")
@Getter
@Setter
public class OrganizationProfile extends Profile {
    private String organizationName;
    private String instagram;
    private String facebook;
    private String website;

    @Override
    public ProfileType getProfileType() {
        return ProfileType.ORGANIZATION;
    }
}
