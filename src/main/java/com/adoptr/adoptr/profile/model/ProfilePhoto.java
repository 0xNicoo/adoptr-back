package com.adoptr.adoptr.profile.model;

import com.adoptr.adoptr.shared.storage.StoredImage;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Foto de un perfil: como máximo una, se reemplaza al subir otra. */
@Entity
@Table(name = "profile_photo")
@Getter
@Setter
public class ProfilePhoto extends StoredImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false, unique = true)
    private Profile profile;
}
