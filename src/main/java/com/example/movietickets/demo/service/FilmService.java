package com.example.movietickets.demo.service;

import com.example.movietickets.demo.model.Blog;
import com.example.movietickets.demo.model.Film;
import com.example.movietickets.demo.model.Rating;
import com.example.movietickets.demo.repository.FilmRepository;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.FileSystemNotFoundException;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class FilmService {
    private final FilmRepository filmRepository;

    // Retrieve all film from the database
    public List<Film> getAllFilms() {
        return filmRepository.findAllByOrderByIdDesc();
    }

    public Page<Film> getAllFilmsForUser(Integer pageNo, Integer pageSize, String sortBy) {
        return filmRepository.findAllFilmsForUser(pageNo, pageSize, sortBy);
    }

    // Lấy film theo id

    public Optional<Film> getFilmById(Long id) {
        return filmRepository.findById(id);
    }

    public Film getFilmByIdFilm(Long id) {
        return filmRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Film not found with id " + id));
    }

    // Thêm film
    public Film addFilm(Film film) throws IOException {

        return filmRepository.save(film);
    }

    public Film updateFilm(Film film) {
        if (film.getId() == null) {
            throw new IllegalArgumentException("Film ID cannot be null for update");
        }
        return filmRepository.save(film);
    }

    // Xóa phim

    public void deleteFilm(Long filmId) {
        filmRepository.deleteById(filmId);
    }

    // find film by ID
    public Film findFilmById(Long id) {
        Optional<Film> product = filmRepository.findById(id);
        return product.orElseThrow(() -> new FileSystemNotFoundException("Product not found with id: " + id));
    }

    // tính số lượng comment trong 1 trang film
    public long getCommentCount(Long filmId) {
        Film film = filmRepository.findById(filmId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid blog Id:" + filmId));
        return film.getRatings().size();
    }

    // find film
    public List<Film> searchFilmsByName(String keyword) {
        return filmRepository.searchFilmByName(keyword);
    }

    // tìm theo id country
    public List<Film> getFilmsByCountryId(Long countryId) {
        return filmRepository.findByCountry_Id(countryId);
    }

    // tìm theo id category
    public List<Film> getFilmsByCategoryId(Long categoryId) {
        
        return filmRepository.findByCategoryId(categoryId);
    }

}
