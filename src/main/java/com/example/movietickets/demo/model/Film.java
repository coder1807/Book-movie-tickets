
package com.example.movietickets.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "Film")
public class Film {
    // @OneToMany(mappedBy = "film", cascade = CascadeType.ALL, orphanRemoval =
    // true)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FILM_ID")
    private Long id;

    @Column(name = "FILM_NAME", nullable = false)
    private String name;

    @Column(name = "TRAILER")
    private String trailer; // Changed to String based on assumption

    @Column(name = "DESCRIPTION", length = 2000)
    private String description; // Changed to String based on assumption

    @Column(name = "POSTER")
    private String poster;

    @Column(name = "DIRECTOR")
    private String director;

    @Column(name = "ACTOR")
    private String actor;

    @Column(name = "OPENING_DAY")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDate openingday;

    @Column(name = "SUBTITLE")
    private String subtitle;

    @Column(name = "DURATION", nullable = false)
    private int duration;

    @Column(name = "LIMIT_AGE")
    private String limit_age;

    @Column(name = "QUANLITY")
    private String quanlity;

    @ManyToOne
    @JoinColumn(name = "COUNTRY_ID")
    private Country country;

    @ManyToOne
    @JoinColumn(name = "CATEGORY_ID")
    private Category category;

    @ManyToMany
    @JoinTable(name = "Film_Category", joinColumns = @JoinColumn(name = "FILM_ID"), inverseJoinColumns = @JoinColumn(name = "CATEGORY_ID"))
    private List<Category> categories;

    @Transient
    private List<Long> categoryIds = new ArrayList<>(); // Khởi tạo danh sách rỗng

    @OneToMany(mappedBy = "film", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rating> ratings;

    @OneToMany(mappedBy = "film", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Schedule> schedules;

    public List<String> getActorList() {
        if (actor == null || actor.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(actor.split("\\s*,\\s*")); // loại bỏ khoảng trắng thua và tách theo dấu phẩy
    }

    @Override
    public String toString() {
        return "Film{id=" + id + ", name='" + name + "'}";
    }
}
