package com.example.movietickets.demo.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
@Entity
@Table(name = "Blog")
public class Blog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BLOG_ID")
    private Long id;

    @Column(name = "BLOG_TITLE", nullable = false)
    private String title;

    @Column(name = "BLOG_CONTENT", nullable = false, length = 2000)
    private String content;

    @Column(name = "BLOG_DAYCREATE", nullable = false)
    private LocalDateTime date;

    @Column(name = "COMBO_POSTER")
    private String poster;

    @OneToMany(mappedBy = "blog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    public String getFormattedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        return date.format(formatter);
    }
}
