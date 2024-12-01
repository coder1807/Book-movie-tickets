package com.example.movietickets.demo.repository;

import com.example.movietickets.demo.model.CardStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CardStudentRepository extends JpaRepository<CardStudent, Long> {
    @Query("select c.isVerified from CardStudent c where c.userId.id = :userId")
    Boolean isVerified(@Param("userId") Long userId);

    @Query("select c.id from CardStudent c where c.userId.id = :userId")
    Optional<Long> findIdByUserId(@Param("userId") Long userId);

    // Kiểm tra thông tin thẻ sinh viên đã tồn tại chưa.
    @Query("SELECT c FROM CardStudent c WHERE c.schoolName = :cardValue AND c.studentId = :mssv")
    Optional<CardStudent> findByCardValueAndMssv(@Param("cardValue") String cardValue, @Param("mssv") String mssv);
}
