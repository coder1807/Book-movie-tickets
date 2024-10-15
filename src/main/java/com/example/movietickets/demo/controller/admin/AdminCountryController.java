package com.example.movietickets.demo.controller.admin;


import com.example.movietickets.demo.model.Country;
import com.example.movietickets.demo.service.CountryService;
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
public class AdminCountryController {
    @Autowired
    private final CountryService countryService;

    // Hiển thị danh sách danh mục
    @GetMapping("/countries")
    public String listCountries(Model model) {
        List<Country> countries = countryService.getAllCountries();
        model.addAttribute("countries", countries);
        model.addAttribute("title", "Danh sách quốc gia");
        return "/admin/country/country-list";
    }

    //gửi response ra view add
    @GetMapping("/countries/add")
    public String showAddForm(Model model) {
        model.addAttribute("country", new Country());
        model.addAttribute("title", "Thêm mới Quốc gia");
        model.addAttribute("actionUrl", "/admin/countries/add");
        return "/admin/country/country-add";
    }

    //gọi phương thức mapp tới form add
    @PostMapping("/countries/add")
    public String addCountries(@Valid Country country, BindingResult result) {
        if (result.hasErrors()) {
            return "/admin/country/country-add";
        }
        countryService.addCountry(country);
        return "redirect:/admin/countries";
    }

    // GET request to show category edit form
    @GetMapping("/countries/edit/{id}")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {
        Country country = countryService.getCountryById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid country Id:" + id));
        model.addAttribute("country", country);
        model.addAttribute("title", "Chỉnh sửa Quốc gia #" + country.getId());
        model.addAttribute("actionUrl", "/admin/countries/edit/"+id);

        return "/admin/country/country-add";
    }

    // POST request to update category
    @PostMapping("/countries/edit/{id}")
    public String updateCategory(@PathVariable("id") Long id, @Valid Country country, BindingResult result, Model model) {
        if (result.hasErrors()) {
            country.setId(id);
            return "/admin/country/country-add";
        }

        countryService.updateCountry(country);
        model.addAttribute("countries", countryService.getAllCountries());
        return "redirect:/admin/countries";
    }

    // GET request for deleting category
    @GetMapping("/countries/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id, Model model) {
        Country country = countryService.getCountryById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quốc gia Id:" + id));

        countryService.deleteCountry(id);
        model.addAttribute("countries", countryService.getAllCountries());
        return "redirect:/admin/countries";
    }
}
