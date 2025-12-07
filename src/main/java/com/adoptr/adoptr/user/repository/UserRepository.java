package com.adoptr.adoptr.user.repository;

import com.adoptr.adoptr.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderAndProviderUserId(String provider, String providerUserId);

}
