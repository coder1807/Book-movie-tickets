package com.example.movietickets.demo.service;

import com.example.movietickets.demo.model.Room;
import com.example.movietickets.demo.model.Seat;
import com.example.movietickets.demo.repository.ScheduleRepository;
import com.example.movietickets.demo.repository.SeatRepository;
import com.example.movietickets.demo.viewmodel.FilmVM;
//import com.example.movietickets.demo.viewmodel.SeatVM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SeatService {


    @Autowired
    private SeatRepository seatRepository;


    public List<Seat> getSeatsByRoomId(Long roomId) {
        return seatRepository.findByRoomId(roomId);
    }
//
//    public Object getSeatsByRoomIdAPI(Long roomId) {
//       List<Seat> list  =  seatRepository.findByRoomId(roomId);
//        return list.stream().map(SeatVM::from).toList();
//    }

    // SeatService.java
    public List<Seat> getSeatsByRoomIdDistinct(Long roomId) {
        // Assuming your repository query or method fetches distinct seats based on roomId
        return seatRepository.findDistinctSeatsByRoomId(roomId);
    }

    public List<Seat> getAllSeats() {
        return seatRepository.findAll();
    }

    public Optional<Seat> getSeatById(Long seatId) {
        return seatRepository.findById(seatId);
    }

    public void addSeat(Seat seat) {
        seatRepository.save(seat);
    }

    public Seat updateSeat(Seat seat) {
        if (seat.getId() == null) {
            throw new IllegalArgumentException("Seat ID cannot be null for update");
        }
        return seatRepository.save(seat);
    }

    public Seat saveSeat(Seat seat) {
        return seatRepository.save(seat);
    }

    public void deleteSeat(Long seatId) {
        if (!seatRepository.existsById(seatId)) {
            throw new IllegalStateException("Seat with ID " + seatId + " does not exist.");
        }
        seatRepository.deleteById(seatId);
    }


}

