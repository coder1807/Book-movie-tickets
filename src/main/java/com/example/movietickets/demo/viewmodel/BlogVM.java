package com.example.movietickets.demo.viewmodel;

import com.example.movietickets.demo.model.Blog;
import com.example.movietickets.demo.model.Category;
import com.example.movietickets.demo.service.BlogService;

import java.time.LocalDateTime;

public record BlogVM(Long id, String blogContent, LocalDateTime blogDate, String blogPoster, String blogTitle) {
    public  static BlogVM from(Blog c){
        return new BlogVM(
                c.getId(),
                c.getContent(),
                c.getDate(),
                c.getPoster(),
                c.getTitle()
        );
    }
}
