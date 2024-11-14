package com.example.movietickets.demo.repository;


import com.example.movietickets.demo.model.Blog;
import com.example.movietickets.demo.model.Film;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogRepository extends PagingAndSortingRepository<Blog, Long>, JpaRepository<Blog, Long> {
    default Page<Blog> findAllBlogForUser(Integer pageNo, Integer pageSize, String sortBy) {
        return findAll(PageRequest.of(pageNo, pageSize, Sort.by(sortBy)));
    }

    @Query("SELECT b FROM Blog b WHERE b.title LIKE %:keyword%")
    List<Blog> searchBlogsByTitle(@Param("keyword") String keyword);

}
