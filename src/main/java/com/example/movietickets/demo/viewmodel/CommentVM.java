package com.example.movietickets.demo.viewmodel;

import com.example.movietickets.demo.model.Comment;

import java.time.LocalDateTime;

public record CommentVM(Long commentID, String commentContent, LocalDateTime commentDate, String fullName) {
    public static  CommentVM from (Comment r){
        return new CommentVM(
                r.getId(),
                r.getContent(),
                r.getDate(),
                r.getUser().getFullname()
        );
    }
}