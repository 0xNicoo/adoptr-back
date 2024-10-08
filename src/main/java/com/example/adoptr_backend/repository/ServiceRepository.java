package com.example.adoptr_backend.repository;

import com.example.adoptr_backend.model.Service;
import com.example.adoptr_backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;



public interface ServiceRepository extends JpaRepository<Service, Long>, JpaSpecificationExecutor<Service> {
    Page<Service> findAllByUser(User user, Pageable pageable);
}
