package com.example.movietickets.demo.controller;


import com.example.movietickets.demo.model.Category;
import com.example.movietickets.demo.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class AboutUsController {

    @Autowired
    private CategoryService categoryService;


    @GetMapping("/about")
    public String about(Model model) {

        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "/about/about-us";
    }
}
