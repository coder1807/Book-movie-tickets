package com.example.movietickets.demo.service;

import com.example.movietickets.demo.model.Blog;
import com.example.movietickets.demo.model.Film;
import com.example.movietickets.demo.repository.BlogRepository;
import com.example.movietickets.demo.viewmodel.BlogVM;
import com.example.movietickets.demo.viewmodel.FilmVM;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.nio.file.FileSystemNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
public class BlogService {
    @Autowired
    private BlogRepository blogRepository;


    //    API Blog Start
    public Object getAllBlogsAPI(){
        List<Blog> list = blogRepository.findAll();
        return  list.stream().map(BlogVM::from).toList();
    }
    //    API Blog End

    public Page<Blog> getAllPostsForUser(Integer pageNo, Integer pageSize, String sortBy) {
        return blogRepository.findAllBlogForUser(pageNo, pageSize, sortBy);
    }

    public List<Blog> getAllPosts() {
        return blogRepository.findAll();
    }

    public Optional<Blog> getPostById(Long id) {
        return blogRepository.findById(id);
    }

    public Blog create(Blog post) {
        return blogRepository.save(post);
    }

    public void updateBlog(@NotNull Blog blog) {
        Blog existingBlog = blogRepository.findById(blog.getId())
                .orElseThrow(() -> new IllegalStateException("Blog with ID " + blog.getId() + " does not exist."));
        existingBlog.setTitle(blog.getTitle());
        existingBlog.setContent(blog.getContent());
        existingBlog.setPoster(blog.getPoster());
        blogRepository.save(existingBlog);
    }

    public void deleteBlog(Long id) {
        if (!blogRepository.existsById(id)) {
            throw new IllegalStateException("Blog with ID " + id + " does not exist.");
        }
        blogRepository.deleteById(id);
    }

    //find blog by ID
    public Blog findBlogWithId(Long id) {
        Optional<Blog> product = blogRepository.findById(id);
        return product.orElseThrow(() -> new FileSystemNotFoundException("Product not found with id: " + id));
    }

    // tính số lượng comment trong 1 trang blog
    public long getCommentCount(Long blogId) {
        Blog blog = blogRepository.findById(blogId).orElseThrow(() -> new IllegalArgumentException("Invalid blog Id:" + blogId));
        return blog.getComments().size();
    }


    // search title
    public List<Blog> searchBlogsByTitle(String keyword) {
        return blogRepository.searchBlogsByTitle(keyword);
    }
}
