package com.example.movietickets.demo.service;

import com.example.movietickets.demo.model.Category;
import com.example.movietickets.demo.model.Room;
import com.example.movietickets.demo.repository.CategoryRepository;
import com.example.movietickets.demo.repository.RoomRepository;
import com.example.movietickets.demo.viewmodel.RoomVM;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class RoomService {
    private final RoomRepository roomRepository;

    public List<Room> getAllRooms() {
        return roomRepository.findAllByOrderByIdDesc();
    }

    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id);
    }


    public void addRoom(Room room) {
        roomRepository.save(room);
    }

    public void updateRoom(@NotNull Room room) {
        Room existingCategory = roomRepository.findById(room.getId())
                .orElseThrow(() -> new IllegalStateException("Room with ID " + room.getId() + " does not exist."));
        existingCategory.setName(room.getName());
        roomRepository.save(existingCategory);
    }

    public void deleteRoomById(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new IllegalStateException("Room with ID " + id + " does not exist.");
        }
        roomRepository.deleteById(id);
    }


    public Object getRoomsByCinemaIdAPI(Long id) {
        List<Room> roomsByCinemaId = roomRepository.findRoomsByCinemaId(id);
        return roomsByCinemaId.stream().map(RoomVM::from).toList();
    }
}
