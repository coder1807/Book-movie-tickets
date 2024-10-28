package com.example.movietickets.demo.service;


import com.example.movietickets.demo.model.SeatType;
import com.example.movietickets.demo.repository.SeatTypeRepository;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class SeatTypeService {

    @Autowired
    private final SeatTypeRepository seatTypeRepository;

    public List<SeatType> getAllSeatTypes() {
        return seatTypeRepository.findAll();
    }

    public Optional<SeatType> getSeatTypeById(Long id) {
        return seatTypeRepository.findById(id);
    }

    public void addSeatType(SeatType seatType) {
        seatTypeRepository.save(seatType);
    }

    public void updateSeatType(@NotNull SeatType seatType) {
        SeatType existingSeatType = seatTypeRepository.findById(seatType.getId())
                .orElseThrow(() -> new IllegalStateException("SeatType with ID " + seatType.getId() + " does not exist."));
        existingSeatType.setType(seatType.getType());
        existingSeatType.setPrice(seatType.getPrice());
        seatTypeRepository.save(existingSeatType);
    }

    public void deleteSeatTypeById(Long id) {
        if (!seatTypeRepository.existsById(id)) {
            throw new IllegalStateException("SeatType with ID " + id + " does not exist.");
        }
        seatTypeRepository.deleteById(id);
    }
}
