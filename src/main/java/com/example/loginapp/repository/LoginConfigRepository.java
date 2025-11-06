package com.example.loginapp.repository;

import com.example.loginapp.model.LoginConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginConfigRepository extends JpaRepository<LoginConfig, Long> {
}
