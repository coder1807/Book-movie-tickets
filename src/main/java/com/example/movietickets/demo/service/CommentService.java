package com.example.movietickets.demo.service;

import com.example.movietickets.demo.DTO.CommentDTO;

import com.example.movietickets.demo.model.*;
import com.example.movietickets.demo.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;


import java.util.List;


@Service
@SessionScope
@AllArgsConstructor
public class CommentService {
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    public Object addCommentAPI(CommentDTO request){
        Blog b = blogRepository.getBlog(request.getBlogId());
        User u = userRepository.getUser(request.getUserId());
        Comment r = new Comment();
        r.setBlog(b);
        r.setUser(u);
        r.setContent(request.getContent());
        commentRepository.save(r);
        return "Add Successfully Comment";
    }

    public List<Comment> getAllCommentsByBlogId(Long BlogId) {
        return commentRepository.findAllByBlogId(BlogId);
    }

    public void saveComment(Comment comment) {
        commentRepository.save(comment);
    }

    public void deleteComment(Long id) {
        if (!commentRepository.existsById(id)) {
            throw new IllegalStateException("Comment with ID " + id + " does not exist.");
        }
        commentRepository.deleteById(id);
    }

}
