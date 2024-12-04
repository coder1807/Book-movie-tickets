package com.example.movietickets.demo.service;


import com.example.movietickets.demo.model.Category;
import com.example.movietickets.demo.model.Film;
import com.example.movietickets.demo.repository.CategoryRepository;
import com.example.movietickets.demo.viewmodel.CategoryVM;
import com.example.movietickets.demo.viewmodel.FilmVM;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import java.util.List;
import java.util.Optional;


@Transactional
@Service
@RequiredArgsConstructor
//khởi tạo constructor tự động


public class CategoryService {
    private final CategoryRepository categoryRepository;
    //    API OBJECT CATEGORIES START
    public  Object getCategoriesAPI(){
        List<Category> list = categoryRepository.findAllByOrderByIdDesc();
        return list.stream().map(CategoryVM::from).toList();
    }
    //    API OBJECT CATEGORIES END



    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByIdDesc();
    }

    public List<Category> findAllById(List<Long> ids) {
        return categoryRepository.findAllById(ids);
    }

    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    public void addCategory(Category category) {
        categoryRepository.save(category);
    }

    public void updateCategory(@NotNull Category category) {
        Category existingCategory = categoryRepository.findById(category.getId())
                .orElseThrow(() -> new IllegalStateException("Category with ID " + category.getId() + " does not exist."));
        existingCategory.setName(category.getName());
        categoryRepository.save(existingCategory);
    }

    public void deleteCategoryById(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new IllegalStateException("Category with ID " + id + " does not exist.");
        }
        categoryRepository.deleteById(id);
    }
}
