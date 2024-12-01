package com.example.movietickets.demo.DTO;

import com.example.movietickets.demo.model.Role;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Bean;

@Getter
@Setter
public class UserDTO {
    private Long id;
    private String username;
    private String fullname;
    private String email;
    private String phone;
    private String address;

    public UserDTO(Long id, String username, String email, String fullname, String phone, String address) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullname = fullname;
        this.phone = phone;
        this.address = address;
    }

}