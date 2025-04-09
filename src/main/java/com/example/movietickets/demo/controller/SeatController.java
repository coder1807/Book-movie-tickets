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

/**
 * Controller xử lý các chức năng liên quan đến ghế ngồi trong rạp chiếu phim
 * Bao gồm: hiển thị danh sách ghế, chọn ghế, kiểm tra trạng thái ghế
 */
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

    /**
     * Lấy danh sách ghế theo phòng chiếu
     * @param roomId ID của phòng chiếu (có thể null)
     * @param model Model để truyền dữ liệu đến view
     * @return View hiển thị danh sách ghế
     */
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

    /**
     * Lấy danh sách ghế theo lịch chiếu và xử lý đặt vé
     * @param scheduleId ID của lịch chiếu
     * @param model Model để truyền dữ liệu đến view
     * @param is_student Loại vé (true: vé học sinh, false: vé người lớn)
     * @return View chọn ghế hoặc chuyển hướng nếu có lỗi
     */
    @GetMapping("/schedules/{scheduleId}")
    public String getSeatsBySchedule(@PathVariable Long scheduleId, Model model,
                                     @RequestParam(required = false) Boolean is_student) {
        // Lấy tất cả các lịch chiếu từ scheduleId
        Optional<Schedule> optionalSchedule = scheduleService.getScheduleById(scheduleId);


        // Nếu giá trị optionalSchedule khác null
        if (optionalSchedule.isPresent()) {
            // Lấy user hiện tại đang đăng nhập
            User currentUser = userService.getCurrentUser();

            Schedule schedule = optionalSchedule.get();
            Film film = schedule.getFilm();
            Long roomId = schedule.getRoom().getId();
            List<Seat> seats = seatService.getSeatsByRoomIdDistinct(roomId);

            // Nhóm ghế theo loại ghế
            Map<String, List<Seat>> seatsByType = seats.stream()
                    .collect(Collectors.groupingBy(seat -> seat.getSeattype().getType()));

            // Thêm thông tin vào model
            Room room = schedule.getRoom();
            Cinema cinema = (room != null) ? room.getCinema() : null;
            String cinemaName = (cinema != null) ? cinema.getName() : "Unknown Cinema";
            String cinemaAddress = (cinema != null) ? cinema.getAddress() : "Unknown Address";


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
            }
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
            model.addAttribute("is_student", is_student);
            return "/seat/seat-choose"; // chuyển đến trang chọn ghế
        } else {
            return "redirect:/404"; // Redirect nếu không tìm thấy lịch chiếu
        }
    }

}