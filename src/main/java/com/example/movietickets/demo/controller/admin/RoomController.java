package com.example.movietickets.demo.controller.admin;


import com.example.movietickets.demo.model.Room;
import com.example.movietickets.demo.model.Cinema;
import com.example.movietickets.demo.service.CategoryService;
import com.example.movietickets.demo.service.CinemaService;
import com.example.movietickets.demo.service.RoomService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@AllArgsConstructor
public class RoomController {
    @Autowired
    private final RoomService roomService;
    @Autowired
    private final CinemaService cinemaService;

    //gửi response ra view add
    @GetMapping("/admin/rooms/add")
    public String showAddForm(Model model) {
        model.addAttribute("room", new Room());
        model.addAttribute("cinemas", cinemaService.getAllCinemas());
        return "/admin/room/room-add";
    }


    //gọi phương thức mapp tới form add
    @PostMapping("/admin/rooms/add")
    public String addRoom(@Valid Room room, BindingResult result) {
        if (result.hasErrors()) {
            return "/admin/room/room-add";
        }
        roomService.addRoom(room);
        return "redirect:/admin/rooms";
    }
    // Hiển thị danh sách danh mục
    @GetMapping("/admin/rooms")
    public String listRooms(Model model) {
        List<Room> listRoom = roomService.getAllRooms();
        model.addAttribute("roomss", listRoom);
        model.addAttribute("title", "Danh sách phòng chiếu");
        return "/admin/room/room-list";
    }
    // GET request to show room edit form
    @GetMapping("/admin/rooms/edit/{id}")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {
        Room room = roomService.getRoomById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid room Id:" + id));
        model.addAttribute("room", room);
        model.addAttribute("cinemas", cinemaService.getAllCinemas());
        return "/admin/room/room-edit";
    }

    // POST request to update room
    @PostMapping("/admin/rooms/edit/{id}")
    public String updateRoom(@PathVariable("id") Long id, @Valid Room room, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("cinemas", cinemaService.getAllCinemas());
            return "/admin/room/room-edit";
        }

        Room existingRoom = roomService.getRoomById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid room Id:" + id));

        // Update the fields
        existingRoom.setName(room.getName());
        existingRoom.setDescription(room.getDescription());
        existingRoom.setCinema(room.getCinema());

        roomService.updateRoom(existingRoom);
        model.addAttribute("rooms", roomService.getAllRooms());
        return "redirect:/admin/rooms";
    }

    // GET request for deleting room
    @GetMapping("/admin/rooms/delete/{id}")
    public String deleteRoom(@PathVariable("id") Long id, Model model) {
        Room room = roomService.getRoomById(id)
                .orElseThrow(() -> new IllegalArgumentException("Phong chieu Id:" + id));

        roomService.deleteRoomById(id);
        model.addAttribute("rooms", roomService.getAllRooms());
        return "redirect:/admin/rooms";
    }
}
