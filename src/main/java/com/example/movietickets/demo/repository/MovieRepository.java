package com.example.movietickets.demo.repository;

import com.example.movietickets.demo.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    Optional<Movie> findByTitle(String title);
    
    List<Movie> findByPriceLessThanEqual(Double price);
    
    List<Movie> findByAvailableSeatsGreaterThan(Integer seats);
    
    List<Movie> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
    
    List<Movie> findByCategory(String category);
    
    List<Movie> findByStatus(Boolean status);
    
    @Query("SELECT m FROM Movie m WHERE m.title LIKE %:keyword% OR m.description LIKE %:keyword%")
    List<Movie> searchMovies(String keyword);
    
    @Query("SELECT COUNT(m) FROM Movie m WHERE m.status = true")
    Long countActiveMovies();
    
    @Query("SELECT SUM(m.price) FROM Movie m WHERE m.status = true")
    Double getTotalRevenue();
} 