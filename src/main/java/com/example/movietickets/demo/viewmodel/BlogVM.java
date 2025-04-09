package com.example.movietickets.demo.viewmodel;

import com.example.movietickets.demo.model.Blog;
import com.example.movietickets.demo.model.Category;
import com.example.movietickets.demo.service.BlogService;

import java.time.LocalDateTime;
import java.util.List;

public record BlogVM(Long id, String blogContent, LocalDateTime blogDate, String blogPoster, String blogTitle, List<CommentVM> comments) {
    public  static BlogVM from(Blog c){
        List<CommentVM> comments = c.getComments().stream().map(CommentVM::from).toList();
        return new BlogVM(
                c.getId(),
                c.getContent(),
                c.getDate(),
                c.getPoster(),
                c.getTitle(),
                comments
        );
    }
}
