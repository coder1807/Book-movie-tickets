package com.example.movietickets.demo.DTO;

import com.example.movietickets.demo.model.Category;
import com.example.movietickets.demo.model.Country;
import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

public class FilmDTO {
    private Long id;
    private String name;
    private String trailer;
    private String description;
    private String poster;
    private String director;
    private String actor;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private Date openingday;
    private String subtitle;
    private int duration;
    private String limit_age;
    private String quanlity;
    private String countryName;
    private String categoryName;
    public FilmDTO(Long id, String name, String trailer, String description, String poster, String director, String actor, Date openingday, String subtitle, int duration, String limit_age,String quanlity, Country countryName, Category categoryName){
        this.id = id;
        this.name = name;
        this.trailer = trailer;
        this.description = description;
        this.poster = poster;
        this.director = director;
        this.actor = actor;
        this.openingday = openingday;
        this.subtitle = subtitle;
        this.duration = duration;
        this.limit_age = limit_age;
        this.quanlity = quanlity;
        this.countryName = countryName.getName();
        this.categoryName = categoryName.getName();
    }
}
