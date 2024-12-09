package com.example.movietickets.demo.service;


import com.example.movietickets.demo.DTO.RatingDTO;
import com.example.movietickets.demo.model.Comment;
import com.example.movietickets.demo.model.Film;
import com.example.movietickets.demo.model.Rating;
import com.example.movietickets.demo.model.User;
import com.example.movietickets.demo.repository.CommentRepository;
import com.example.movietickets.demo.repository.FilmRepository;
import com.example.movietickets.demo.repository.RatingRepository;
import com.example.movietickets.demo.repository.UserRepository;
import com.example.movietickets.demo.viewmodel.RatingVM;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.time.LocalDateTime;
import java.util.List;

@Service
@SessionScope
@AllArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;
    // Rating API Start
    public Object addRatingAPI(RatingDTO request){
        Film f = filmRepository.getFilm(request.getFilmId());
        User u = userRepository.getUser(request.getUserId());
        Rating r = new Rating();
        r.setFilm(f);
        r.setUser(u);
        r.setDate(LocalDateTime.now());
        r.setStar(request.getRatingNumber());
        r.setContent(request.getContent());
        ratingRepository.save(r);
        return "Add Successfully Rating";
    }

    // Rating API END

    public List<Rating> getAllRatingByFilmId(Long FilmId) {
        return ratingRepository.findAllByFilmId(FilmId);
    }

    public void saveRating(Rating rating) {
        ratingRepository.save(rating);
    }

    public void deleteRating(Long id) {
        if (!ratingRepository.existsById(id)) {
            throw new IllegalStateException("Rating with ID " + id + " does not exist.");
        }
        ratingRepository.deleteById(id);
    }

    public boolean hasUserRatedFilm(String username, Long filmId) {
        return ratingRepository.existsByUserUsernameAndFilmId(username, filmId);
    }

    public Double getAverageRating(Long filmId) {
        return ratingRepository.findAverageRatingByFilmId(filmId);
    }
}
