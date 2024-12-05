package com.example.movietickets.demo.DTO;

import com.example.movietickets.demo.model.Role;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cglib.core.Local;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;

@Getter
@Setter
public class UserDTO {
    private Long id;
    private String username;
    private String fullname;
    private String email;
    private String phone;
    private String address;
    private LocalDate birthday;

    public UserDTO(Long id, String username, String email, String fullname, String phone, String address, LocalDate birthday) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullname = fullname;
        this.phone = phone;
        this.address = address;
        this.birthday = birthday;
    }



}