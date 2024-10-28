package com.example.movietickets.demo.repository;

import com.example.movietickets.demo.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IRoleRepository extends JpaRepository<Role, Long> {
    Role findRoleById(Long id);
}