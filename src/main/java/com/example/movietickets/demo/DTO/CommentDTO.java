package com.example.movietickets.demo.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentDTO {
    private Long blogId;
    private Long userId;
    private String content;
}
