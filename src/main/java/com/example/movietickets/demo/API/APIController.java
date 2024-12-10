package com.example.movietickets.demo.API;

import com.example.movietickets.demo.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = "/api", produces = "application/json; charset=UTF-8")
public class APIController {
    @Autowired
    private FilmService filmService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private CountryService countryService;
    @Autowired
    private ComboFoodService comboFoodService;
    @Autowired
    private CinemaService cinemaService;

    @Autowired
    private SeatService seatService;

    //   API MOVIES START
    @GetMapping("/movies")
    public ResponseEntity<Object> GetMovies() {
        Object o = filmService.allMoviesAPI();
        return ResponseEntity.ok(o);
    }

    @GetMapping("/movie")
    public ResponseEntity<Object> getMovie(@RequestParam Map<String, String> params) {
        if (params.containsKey("name")) {
            String movieName = params.get("name");
            Object o = filmService.getMovieByNameAPI(movieName);
            return ResponseEntity.ok(o);
        } else if (params.containsKey("id")) {
            Long movieID = Long.parseLong(params.get("id"));
            Object o = filmService.getMovieByIdApi(movieID);
            return ResponseEntity.ok(o);
        }
        return ResponseEntity.badRequest().body("Invalid parameters");
    }
//    @PostMapping("movie")
//    public  ResponseEntity<Object> addMovie()
//    {
//
//        return ResponseEntity.ok(o);
//    }
    //   API MOVIES END

    //   API Categories START
    @GetMapping("/categories")
    public ResponseEntity<Object> getCategories() {
        Object o = categoryService.getCategoriesAPI();
        return ResponseEntity.ok(o);
    }
    //   API Categories END

    // API Country START
    @GetMapping("/countries")
    public ResponseEntity<Object> getCountries() {
        Object o = countryService.getCountriesAPI();
        return ResponseEntity.ok(o);
    }
    // API Country END

    // API Food Start
    @GetMapping("/foods")
    public ResponseEntity<Object> getFoods() {
        Object o = comboFoodService.getFoodsAPI();
        return ResponseEntity.ok(o);
    }

    @GetMapping("/food")
    public ResponseEntity<Object> getFood(@RequestParam Map<String, String> params) {
        Long foodID = Long.parseLong(params.get("foodId"));
        Object o = comboFoodService.getFoodByIdApi(foodID);
        return ResponseEntity.ok(o);
    }

    // API Food End
    // API Cinemas Start
    @GetMapping("/cinemas")
    public ResponseEntity<Object> getCinemas() {
        Object o = cinemaService.getCinemasAPI();
        return ResponseEntity.ok(o);
    }

    @GetMapping("/cinema")
    public ResponseEntity<Object> getCinema(@RequestParam Map<String, String> params) {
        Long cinemaID = Long.parseLong(params.get("cinemaId"));
        Object o = cinemaService.getCinemaByIdAPI(cinemaID);
        return ResponseEntity.ok(o);
    }
    // API Cinemas End

    // API Seat Start
    @GetMapping("/seats")
    public ResponseEntity<Object> getSeats(@RequestParam Map<String, String> params) {
        Long roomId = Long.parseLong(params.get("roomId"));
        Object o = seatService.getSeatsByRoomIdAPI(roomId);
        return ResponseEntity.ok(o);
    }
    // API Cinemas End
    // API Schedule Start
//    @GetMapping("/schedules")
//    public ResponseEntity<Object> getSchedules(@RequestParam Map<String, String> params){
//        Long cina
//    }
    // API Schedule End
}
