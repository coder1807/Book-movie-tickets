package com.example.movietickets.demo.viewmodel;

import com.example.movietickets.demo.model.*;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.util.List;
import java.util.Date;
import java.util.stream.Collectors;

public record FilmVM(Long id, String name, String trailer, String description, String poster, String director,
                     String actor, LocalDate openingay, String subtitle, int duration, String limit_age,
                     String quanlity, String countryName, Double ratingAvg, List<Category> categories,
                     List<RatingVM> ratings, List<CinemaWithRooms> cinemas) {
    public static FilmVM from(Film f) {
        List<Category> categories = f.getCategories()
                .stream()
                .map(c -> new Category(
                                c.getName()
                        )
                ).toList();
        List<RatingVM> ratings = f.getRatings().stream().map(RatingVM::from).toList();
        double averageRating = ratings.stream()
                .mapToDouble(RatingVM::ratingNumber)
                .average()
                .orElse(0.0);
        List<CinemaWithRooms> cinemas = f.getSchedules().stream()
                .map(Schedule::getRoom)
                .distinct()
                .collect(Collectors.groupingBy(Room::getCinema))
                .entrySet().stream()
                .map(entry -> new CinemaWithRooms(
                        entry.getKey().getId(),
                        entry.getKey().getName(),
                        entry.getKey().getAddress(),
                        entry.getValue().stream()
                                .map(room -> new RoomVM(
                                        room.getId(),
                                        room.getName(),
                                        room.getSchedules().stream().map(ScheduleVM.detail::from).toList()
                                ))
                                .toList()
                ))
                .toList();
        return new FilmVM(
                f.getId(),
                f.getName(),
                f.getTrailer(),
                f.getDescription(),
                f.getPoster(),
                f.getDirector(),
                f.getActor(),
                f.getOpeningday(),
                f.getSubtitle(),
                f.getDuration(),
                f.getLimit_age(),
                f.getQuanlity(),
                f.getCountry().getName(),
                averageRating,
                categories,
                ratings,
                cinemas
        );
    }

    private record Category(String categoryName) {
    }

    public record CinemaWithRooms(Long id, String name, String address, List<RoomVM> rooms) {
    }

    public record filmCinema(Long id, String name, String poster, String director, String actor, LocalDate openingDay,
                             String subtitle, int duration, String limit_age, String quanlity, String countryName,
                             List<Category> categories, List<ScheduleVM.detail> schedules) {
        public static filmCinema from(Film f) {
            List<Category> categories = f.getCategories()
                    .stream()
                    .map(c -> new Category(
                                    c.getName()
                            )
                    ).toList();
            List<ScheduleVM.detail> schedules = f.getSchedules().stream().map(ScheduleVM.detail::from).toList();
            return new filmCinema(
                    f.getId(),
                    f.getName(),
                    f.getPoster(),
                    f.getDirector(),
                    f.getActor(),
                    f.getOpeningday(),
                    f.getSubtitle(),
                    f.getDuration(),
                    f.getLimit_age(),
                    f.getQuanlity(),
                    f.getDirector(),
                    categories,
                    schedules
            );
        }
    }
}
