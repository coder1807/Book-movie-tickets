package com.example.movietickets.demo.viewmodel;

import com.example.movietickets.demo.model.Film;
import com.example.movietickets.demo.model.User;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public record UserVM(Long id, String username, String fullname, String email, String phone,
                     String address, LocalDate birthday, List<Long> favoriteFilmIds) {

    public static  UserVM from (User u){
        // Lấy danh sách ID phim yêu thích từ User
        List<Long> favoriteFilmIds = u.getFavoriteFilms().stream()
                .map(Film::getId)
                .collect(Collectors.toList());
        return new UserVM(
                u.getId(),
                u.getUsername(),
                u.getFullname(),
                u.getEmail(),
                u.getPhone(),
                u.getAddress(),
                u.getBirthday(),
                favoriteFilmIds
        );
    }
}