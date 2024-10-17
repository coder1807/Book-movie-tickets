package com.example.movietickets.demo.repository;

import com.example.movietickets.demo.model.Booking;
import com.example.movietickets.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT COUNT(id) AS total FROM Booking")
    Long getCountBooking();

    @Query("SELECT SUM(b.price) FROM Booking b")
    Long getTotalPrice();
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.user.id = :user_id")
    Long getCountBookingByUser(@Param("user_id") long user_id);
    @Query("SELECT b from Booking b WHERE b.user.id = :user_id ORDER BY b.createAt DESC")
    List<Booking> findByUser(@Param("user_id") long user_id);

    @Query("SELECT SUM(b.price) FROM Booking b where b.user.id = :user_id")
    Long getTotalPriceByUser(@Param("user_id") long user_id);

    List<Booking> findAllWithComboFoodByUser(User user);
}
