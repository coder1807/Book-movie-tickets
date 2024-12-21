package com.example.movietickets.demo.service;

import com.example.movietickets.demo.model.Cinema;
import com.example.movietickets.demo.repository.CinemaRepository;
import com.example.movietickets.demo.viewmodel.CinemaVM;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CinemaService {

    private final CinemaRepository cinemaRepository;
//    API OBJECT CINEMA API START
    public  Object getCinemasAPI(){
        List<Cinema> list = cinemaRepository.findAllByOrderByIdDesc();
         return list.stream().map(CinemaVM::from).toList();
    }

    public Object getCinemaByIdAPI(Long cinemaID) {
        List<Cinema> list = cinemaRepository.findCinemaByIdQuery(cinemaID);
        return list.stream().map(CinemaVM::from).toList();
    }

    public Object getCinemaByScheduleId(Long scheduleId){
        List<Cinema> list = cinemaRepository.findCinemaByScheduleId(scheduleId);
        return list.stream().map(CinemaVM::from).toList();
    }
//    API OBJECT CINEMA API END

    public List<Cinema> getAllCinemas() {
        return cinemaRepository.findAllByOrderByIdDesc();
    }
    public Optional<Cinema> getCinemaById(Long id) {
        return cinemaRepository.findById(id);
    }

    public void addCinema(Cinema cinema) {
        cinemaRepository.save(cinema);
    }

    public void updateCinema(@NotNull Cinema cinema) {
        Cinema existingCinema = cinemaRepository.findById(cinema.getId())
                .orElseThrow(() -> new IllegalStateException("Cinema with ID " + cinema.getId() + " does not exist."));
        existingCinema.setName(cinema.getName());
        existingCinema.setAddress(cinema.getAddress());
        existingCinema.setMap(cinema.getMap());
        cinemaRepository.save(existingCinema);
    }

    public void deleteCinemaById(Long id) {
        if (!cinemaRepository.existsById(id)) {
            throw new IllegalStateException("Cinema with ID " + id + " does not exist.");
        }
        cinemaRepository.deleteById(id);
    }


}