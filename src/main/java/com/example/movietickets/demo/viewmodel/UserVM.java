package com.example.movietickets.demo.viewmodel;

import com.example.movietickets.demo.model.Rating;
import com.example.movietickets.demo.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserVM(Long id, String username, String fullname, String email, String phone,String address, LocalDate birthday) {
    public static  UserVM from (User u){
        return new UserVM(
                u.getId(),
                u.getUsername(),
                u.getFullname(),
                u.getEmail(),
                u.getPhone(),
                u.getAddress(),
                u.getBirthday()
        );
    }

}