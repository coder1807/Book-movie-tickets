package com.example.movietickets.demo.repository;

import com.example.movietickets.demo.model.CardStudent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardStudentRepository extends JpaRepository<CardStudent, Long> {
}
