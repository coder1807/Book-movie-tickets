package com.example.movietickets.demo.service;


import com.example.movietickets.demo.model.Category;
import com.example.movietickets.demo.model.Country;
import com.example.movietickets.demo.repository.CountryRepository;
import com.example.movietickets.demo.viewmodel.CategoryVM;
import com.example.movietickets.demo.viewmodel.CountryVM;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
//khởi tạo constructor tự động

public class CountryService {
    private final CountryRepository countryRepository;

    //  API OBJECT COUNTRIES START
    public  Object getCountriesAPI(){
        List<Country> list = countryRepository.findAllByOrderByIdDesc();
        return list.stream().map(CountryVM::from).toList();
    }
    //  API OBJECT COUNTRIES END
    public List<Country> getAllCountries() {
        return countryRepository.findAllByOrderByIdDesc();
    }

    public Optional<Country> getCountryById(Long id) {
        return countryRepository.findById(id);
    }

    public void addCountry(Country country) {
        countryRepository.save(country);
    }

    public void updateCountry(@NotNull Country country) {
        Country existingCategory = countryRepository.findById(country.getId())
                .orElseThrow(() -> new IllegalStateException("Country with ID " + country.getId() + " does not exist."));
        existingCategory.setName(country.getName());
        countryRepository.save(existingCategory);
    }

    public void deleteCountry(Long id) {
        if (!countryRepository.existsById(id)) {
            throw new IllegalStateException("Country with ID " + id + " does not exist.");
        }
        countryRepository.deleteById(id);
    }

}
