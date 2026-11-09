package com.adoptr.adoptr.profile.repository;

import com.adoptr.adoptr.profile.model.ProfilePhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfilePhotoRepository extends JpaRepository<ProfilePhoto, Long> {

    Optional<ProfilePhoto> findByProfileId(Long profileId);
}
