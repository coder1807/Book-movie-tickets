package com.example.movietickets.demo.service;

import com.example.movietickets.demo.model.Category;
import com.example.movietickets.demo.model.ComboFood;
import com.example.movietickets.demo.model.Country;
import com.example.movietickets.demo.repository.ComboFoodRepository;
import com.example.movietickets.demo.viewmodel.CategoryVM;
import com.example.movietickets.demo.viewmodel.ComboFoodVM;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.List;
import java.util.Optional;

@Service
@SessionScope
@AllArgsConstructor
public class ComboFoodService {

    private final ComboFoodRepository comboFoodRepository;

    //API OBJECT FOOD START
    public  Object getFoodsAPI(){
        List<ComboFood> list = comboFoodRepository.findAllByOrderByIdDesc();
        return list.stream().map(ComboFoodVM::from).toList();
    }

    //API OBJECT FOOD END

    public List<ComboFood> getAllComboFood() {
        return comboFoodRepository.findAllByOrderByIdDesc();
    }

    public Optional<ComboFood> getComboFoodById(Long id) {
        return comboFoodRepository.findById(id);
    }

    public void addComboFood(ComboFood comboFood) {
        comboFoodRepository.save(comboFood);
    }

    public void updateComboFood(@NotNull ComboFood comboFood) {
        ComboFood existingCategory = comboFoodRepository.findById(comboFood.getId())
                .orElseThrow(
                        () -> new IllegalStateException("Country with ID " + comboFood.getId() + " does not exist."));
        existingCategory.setComboName(comboFood.getComboName());
        comboFoodRepository.save(existingCategory);
    }

    public void deleteComboFood(Long id) {
        if (!comboFoodRepository.existsById(id)) {
            throw new IllegalStateException("Country with ID " + id + " does not exist.");
        }
        comboFoodRepository.deleteById(id);
    }


}
