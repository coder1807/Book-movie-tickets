package com.example.movietickets.demo.repository;

import com.example.movietickets.demo.model.ComboFood;
import com.example.movietickets.demo.model.Film;
import com.example.movietickets.demo.model.Category;
import com.example.movietickets.demo.model.Country;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FilmRepository extends PagingAndSortingRepository<Film, Long>, JpaRepository<Film, Long> {
    default Page<Film> findAllFilmsForUser(Integer pageNo, Integer pageSize, String sortBy) {
        return findAll(PageRequest.of(pageNo, pageSize, Sort.by(sortBy)));
    }

    @Query("SELECT cf FROM Film cf ORDER BY cf.id DESC")
    List<Film> findAllByOrderByIdDesc();

    @Query("SELECT f FROM Film f WHERE f.name LIKE %:keyword%")
    List<Film> searchFilmByName(@Param("keyword") String keyword);

    @Query("SELECT f FROM Film f WHERE f.id =:filmID")
    Film getFilm(Long filmID);

    @Query("SELECT f FROM Film f WHERE f.id =:filmID")
    List<Film> findByMovie_Id(Long filmID);

    List<Film> findByCountry_Id(Long countryId);
    @Query("SELECT f FROM Film f JOIN f.categories c WHERE c.id = :categoryId")
    List<Film> findByCategoryId(Long categoryId);

    @Query("SELECT r FROM Schedule s JOIN s.film r WHERE s.id = :scheduleId")
    Film findFilmByScheduleId(Long scheduleId);

    Optional<Film> findByName(String name);
    List<Film> findByCategory(Category category);
    List<Film> findByCountry(Country country);
    List<Film> findByOpeningdayBetween(LocalDate startDate, LocalDate endDate);
}
