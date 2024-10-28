package com.example.movietickets.demo.controller.admin;

import com.example.movietickets.demo.model.Category;
import com.example.movietickets.demo.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminCategoryController {
    @Autowired
    private final CategoryService categoryService;

    // Hiển thị danh sách danh mục
    @GetMapping("/categories")
    public String listCategories(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("title", "Danh sách thể loại");
        return "/admin/category/category-list";
    }

    //gửi response ra view add
    @GetMapping("/categories/add")
    public String showAddForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("title", "Thêm mới Thể loại");
        model.addAttribute("actionUrl", "/admin/categories/add");
        return "/admin/category/category-add";
    }

    //gọi phương thức mapp tới form add
    @PostMapping("/categories/add")
    public String addCategory(@Valid Category category, BindingResult result) {
        if (result.hasErrors()) {
            return "/admin/category/category-add";
        }
        categoryService.addCategory(category);
        return "redirect:/admin/categories";
    }

    // GET request to show category edit form
    @GetMapping("/categories/edit/{id}")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category Id:" + id));
        model.addAttribute("category", category);
        model.addAttribute("title", "Chỉnh sửa Thể loại #" + category.getId());
        model.addAttribute("actionUrl", "/admin/categories/edit/" + id);

        return "/admin/category/category-add";
    }

    // POST request to update category
    @PostMapping("/categories/edit/{id}")
    public String updateCategory(@PathVariable("id") Long id, @Valid Category category, BindingResult result, Model model) {
        if (result.hasErrors()) {
            category.setId(id);
            return "/admin/category/category-add";
        }

        categoryService.updateCategory(category);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "redirect:/admin/categories";
    }

    // GET request for deleting category
    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id, Model model) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new IllegalArgumentException("Thể loại Id:" + id));

        categoryService.deleteCategoryById(id);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "redirect:/admin/categories";
    }
}
