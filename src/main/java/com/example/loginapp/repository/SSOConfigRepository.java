package com.example.loginapp.repository;

import com.example.loginapp.model.SSOConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SSOConfigRepository extends JpaRepository<SSOConfig, Long> {
    // Fetch the most recent SSO configuration entry
    SSOConfig findTopByOrderByIdDesc();
}
