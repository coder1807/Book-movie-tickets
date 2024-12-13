package com.example.movietickets.demo.service;

import com.example.movietickets.demo.model.Film;
import com.example.movietickets.demo.model.Rating;
import com.example.movietickets.demo.repository.FilmRepository;
import com.example.movietickets.demo.repository.RatingRepository;
import com.example.movietickets.demo.viewmodel.FilmVM;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.FileSystemNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

import com.example.movietickets.demo.model.Category;

@AllArgsConstructor
@Service
public class FilmService {
    private final FilmRepository filmRepository;
    private final RatingRepository ratingRepository;

    //    API START
    public Object allMoviesAPI() {
        List<Film> list = filmRepository.findAllByOrderByIdDesc();
        return list.stream().map(FilmVM::from).toList();
    }

    public Object getMovieByNameAPI(String movieName) {
        List<Film> list = filmRepository.searchFilmByName(movieName);
        return list.stream().map(FilmVM::from).toList();
    }

    public Object getMovieByIdApi(Long movieID) {
        List<Film> list = filmRepository.findByMovie_Id(movieID);
        List<Rating> listRating = ratingRepository.findRatingByFilmId(movieID);
        return list.stream().map(FilmVM::from).toList();
    }
//    API END

    // Retrieve all film from the database
    public List<Film> getAllFilms() {
        return filmRepository.findAllByOrderByIdDesc();
    }

    public Page<Film> getAllFilmsForUser(Integer pageNo, Integer pageSize, String sortBy) {
        return filmRepository.findAllFilmsForUser(pageNo, pageSize, sortBy);
    }

    // Lấy film theo id

    public Optional<Film> getFilmById(Long id) {
        return filmRepository.findById(id);
    }

    public Film getFilmByIdFilm(Long id) {
        return filmRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Film not found with id " + id));
    }

    // Thêm film
    public Film addFilm(Film film) throws IOException {

        return filmRepository.save(film);
    }

    public Film updateFilm(Film film) {
        if (film.getId() == null) {
            throw new IllegalArgumentException("Film ID cannot be null for update");
        }
        return filmRepository.save(film);
    }

    // Xóa phim

    public void deleteFilm(Long filmId) {
        filmRepository.deleteById(filmId);
    }

    // find film by ID
    public Film findFilmById(Long id) {
        Optional<Film> product = filmRepository.findById(id);
        return product.orElseThrow(() -> new FileSystemNotFoundException("Product not found with id: " + id));
    }

    // tính số lượng comment trong 1 trang film
    public long getCommentCount(Long filmId) {
        Film film = filmRepository.findById(filmId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid blog Id:" + filmId));
        return film.getRatings().size();
    }

    // find film
    public List<Film> searchFilmsByName(String keyword) {
        return filmRepository.searchFilmByName(keyword);
    }

    // tìm theo id country
    public List<Film> getFilmsByCountryId(Long countryId) {
        return filmRepository.findByCountry_Id(countryId);
    }

    // tìm theo id category
    public List<Film> getFilmsByCategoryId(Long categoryId) {

        return filmRepository.findByCategoryId(categoryId);
    }


    // Webhook for dialogflow
    public Map<String, Object> getSuggestedMovies() {
        List<Film> movies = filmRepository.findAll();
        if (movies.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("fulfillmentMessages", Arrays.asList(
                    new HashMap<String, Object>() {{
                        put("text", new HashMap<String, Object>() {{
                            put("text", Arrays.asList("Hiện tại không có thể loại nào trong danh sách."));
                        }});
                    }}
            ));
            return response;
        }

        Random random = new Random();
        int numberOfMovies = random.nextInt(1) + 1;
        Collections.shuffle(movies);
        List<Film> selectedMovies = movies.subList(0, Math.min(numberOfMovies, movies.size()));

        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> fulfillmentMessages = new ArrayList<>();

        // Thêm text giới thiệu
        fulfillmentMessages.add(new HashMap<String, Object>() {{
            put("text", new HashMap<String, Object>() {{
                put("text", Arrays.asList("Dưới đây là phim bạn có thể tham khảo:"));
            }});
        }});

        // Tạo rich content cho mỗi phim
        List<List<Map<String, Object>>> richContentList = new ArrayList<>();

        for (Film film : selectedMovies) {
            List<Map<String, Object>> richContent = new ArrayList<>();

            // Thêm thông tin phim dưới dạng text
            Map<String, Object> infoCard = new HashMap<>();
            infoCard.put("type", "info");

            StringBuilder info = new StringBuilder();
            info.append("🎬 *").append(film.getName()).append("*\n");

            if (film.getCategories() != null && !film.getCategories().isEmpty()) {
                info.append("🎭 Thể loại: ");
                String categories = film.getCategories().stream()
                        .map(Category::getName)
                        .collect(Collectors.joining(", "));
                info.append(categories).append("\n");
            }

            if (film.getDuration() > 0) {
                info.append("⏱️ Thời lượng: ").append(film.getDuration()).append(" phút\n");
            }

            if (film.getDescription() != null && !film.getDescription().isEmpty()) {
                info.append("📝 Tóm tắt: ").append(film.getDescription()).append("\n");
            }

            infoCard.put("title", info.toString());
            richContent.add(infoCard);

            // Thêm poster
            if (film.getPoster() != null && !film.getPoster().isEmpty()) {
                Map<String, Object> imageCard = new HashMap<>();
                imageCard.put("type", "image");
                imageCard.put("rawUrl", film.getPoster());
                imageCard.put("accessibilityText", film.getName() + " poster");
                richContent.add(imageCard);
            }

            richContentList.add(richContent);
        }

        // Thêm rich content vào fulfillmentMessages
        fulfillmentMessages.add(new HashMap<>() {{
            put("payload", new HashMap<String, Object>() {{
                put("richContent", richContentList);
            }});
        }});

        response.put("fulfillmentMessages", fulfillmentMessages);
        return response;
    }
}
