package com.example.adoptr_backend.repository;

import com.example.adoptr_backend.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
