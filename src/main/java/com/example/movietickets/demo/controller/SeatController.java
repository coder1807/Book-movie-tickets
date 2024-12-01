package com.example.movietickets.demo.controller;

import com.example.movietickets.demo.model.*;
import com.example.movietickets.demo.repository.BookingDetailRepository;
import com.example.movietickets.demo.repository.BookingRepository;
import com.example.movietickets.demo.repository.CardStudentRepository;
import com.example.movietickets.demo.service.*;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RequestMapping("/seats")
@AllArgsConstructor
@Controller("userSeatController")
public class SeatController {
    @Autowired
    private SeatService seatService;

    @Autowired
    private ScheduleServiceImpl scheduleService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private SeatTypeService seatTypeService;

    @Autowired
    private BookingDetailService bookingDetailService;

    private BookingDetailRepository bookingDetailRepository;

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private UserService userService;
    @Autowired
    private CardStudentRepository cardStudentRepository;

    @GetMapping
    public String getSeatsByRoomId(@RequestParam(value = "roomId", required = false) Long roomId, Model model) {
        List<Seat> seats;
        if (roomId != null) {
            seats = seatService.getSeatsByRoomIdDistinct(roomId);
        } else {
            seats = seatService.getAllSeats();
        }
        model.addAttribute("seats", seats);
        model.addAttribute("rooms", roomService.getAllRooms());
        model.addAttribute("selectedRoomId", roomId);
        return "/seat/seat-list";
    }

    @GetMapping("/schedules/{scheduleId}")
    public String getSeatsBySchedule(@PathVariable Long scheduleId, Model model,
                                     @RequestParam(required = false) Boolean is_student) {
        Optional<Schedule> optionalSchedule = scheduleService.getScheduleById(scheduleId);
        User currentUser = userService.getCurrentUser();
        if (optionalSchedule.isPresent()) {
            Schedule schedule = optionalSchedule.get();
            Film film = schedule.getFilm();
            Long roomId = schedule.getRoom().getId();
            List<Seat> seats = seatService.getSeatsByRoomIdDistinct(roomId);

            // Lấy danh sách các BookingDetail của suất chiếu hiện tại
            List<BookingDetail> bookingDetails = bookingDetailService.getBookingDetailsByScheduleId(scheduleId);

            // Đánh dấu ghế đã được đặt cho suất chiếu hiện tại
            for (Seat seat : seats) {
                seat.setStatus("empty"); // Đặt mặc định là 'available'
                for (BookingDetail bookingDetail : bookingDetails) {
                    if (bookingDetail.getSeat().getId().equals(seat.getId())) {
                        seat.setStatus("booked"); // Đánh dấu là 'booked' nếu có trong BookingDetail của suất chiếu hiện
                        // tại
                        break;
                    }
                }
            }

            // Nhóm ghế theo loại ghế
            Map<String, List<Seat>> seatsByType = seats.stream()
                    .collect(Collectors.groupingBy(seat -> seat.getSeattype().getType()));

            // Thêm thông tin vào model
            String cinemaName = schedule.getRoom().getCinema().getName();
            String cinemaAddress = schedule.getRoom().getCinema().getAddress();
            String roomName = schedule.getRoom().getName();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            String currentTime = LocalTime.now().format(formatter);

            List<Category> categories = categoryService.getAllCategories();

            model.addAttribute("categories", categories);
            model.addAttribute("currentTime", currentTime);
            model.addAttribute("seats", seats);
            model.addAttribute("film", film);
            model.addAttribute("schedule", schedule);
            model.addAttribute("selectedScheduleId", scheduleId);
            model.addAttribute("selectedRoomId", roomId);
            model.addAttribute("seatsByType", seatsByType);
            model.addAttribute("cinemaName", cinemaName);
            model.addAttribute("cinemaAddress", cinemaAddress);
            model.addAttribute("roomName", roomName);
            if (is_student != null && cardStudentRepository.isVerified(currentUser.getId()) == null) { // Kiểm tra nếu là sinh viên nhưng chưa xác thực thì trả về lỗi
                return "redirect:/error/404";
            } else {
                is_student = is_student != null;
            }
            model.addAttribute("is_student", is_student);
            return "/seat/seat-choose"; // chuyển đến trang chọn ghế
        } else {
            return "redirect:/404"; // Redirect nếu không tìm thấy lịch chiếu
        }
    }

    // Lấy danh sách ghế đã được đặt dựa trên scheduleId
    // List<BookingDetail> bookingDetails =
    // bookingDetailService.getBookingDetailsByScheduleId(scheduleId);
    // Set<Long> bookedSeatIds = bookingDetails.stream()
    // .map(BookingDetail::getSeat)
    // .map(Seat::getId)
    // .collect(Collectors.toSet());
}