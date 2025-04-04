package com.example.movietickets.demo.repository;

import com.example.movietickets.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository<User, Long> {
    @Query("SELECT cf FROM User cf ORDER BY cf.id DESC")
    List<User> findAllByOrderByIdDesc();

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByUsername(String username);

    Optional<User> findByUsernameAndPassword(String username, String password);

    @Query("SELECT ROUND(SUM(b.price) / 1000) FROM Booking b WHERE b.user.id = :user_id AND YEAR(b.createAt) = YEAR(CURRENT_DATE)")
    Long getPointUser(@Param("user_id") long user_id);

//    String getUserType(long user_id);
}