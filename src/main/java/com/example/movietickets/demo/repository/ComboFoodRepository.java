package com.example.movietickets.demo.repository;

import com.example.movietickets.demo.model.ComboFood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComboFoodRepository extends JpaRepository<ComboFood, Long> {
    @Query("SELECT cf FROM ComboFood cf ORDER BY cf.id DESC")
    List<ComboFood> findAllByOrderByIdDesc();

    @Query("SELECT cf FROM ComboFood cf WHERE cf.id =:foodId")
    List<ComboFood> findFoodByIdQuery(Long foodId);
}
