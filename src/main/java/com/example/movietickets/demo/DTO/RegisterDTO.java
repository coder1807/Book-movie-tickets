package com.example.movietickets.demo.DTO;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RegisterDTO{
    private String username;
    private String password;
    private String email;
    private LocalDate birthday;
}
