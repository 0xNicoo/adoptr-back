package com.example.adoptr_backend.repository;

import com.example.adoptr_backend.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    void deleteByUserId(Long userId);

    List<Notification> getAllByUserId(Long userId);
}
