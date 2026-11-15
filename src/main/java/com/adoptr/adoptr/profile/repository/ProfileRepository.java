package com.adoptr.adoptr.profile.repository;

import com.adoptr.adoptr.profile.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    /** Para los listados: los perfiles de varios usuarios en una sola consulta. */
    List<Profile> findByUserIdIn(Collection<Long> userIds);
}
