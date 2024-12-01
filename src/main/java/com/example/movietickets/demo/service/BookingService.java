package com.example.movietickets.demo.service;

import com.example.movietickets.demo.model.*;
import com.example.movietickets.demo.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@SessionScope
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingDetailRepository bookingDetailRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ScheduleServiceImpl scheduleService;

    @Autowired
    private ComboFoodService comboFoodService;

    @Autowired
    private UserService userService;

    private final ComboFoodRepository comboFoodRepository;

    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository, ComboFoodRepository comboFoodRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.comboFoodRepository = comboFoodRepository;
        this.userRepository = userRepository;
    }

    public List<Booking> getAll() {
        return bookingRepository.findAll();
    }

    public Optional<Booking> getBookingId(Long id) {
        return bookingRepository.findById(id);
    }

    public Long getCountBooking() {
        return bookingRepository.getCountBooking();
    }

    public Long getTotalPrice() {
        return bookingRepository.getTotalPrice();
    }

    public Long getCountBookingByUser(long user_id) {
        return bookingRepository.getCountBookingByUser(user_id);
    }

    public Long getTotalPriceByUser(long user_id) {
        Long totalPrice = bookingRepository.getTotalPriceByUser(user_id);
        return totalPrice != null ? totalPrice : 0;
    }

    public List<Seat> getSeatsFromSymbolsAndRoom(List<String> seatSymbols, Room room) {
        return seatRepository.findBySymbolInAndRoom(seatSymbols, room);
    }

    public List<Booking> getAllBookings() {
        // Thay đổi phương thức để lấy các booking của người dùng hiện tại
        return bookingRepository.findAll();
    }

    public List<Booking> getBookingsByUser(long user_id) {
        return bookingRepository.findByUser(user_id);
    }


    public Booking saveBookingWithCombo(Long userId, Long comboFoodId, Booking booking) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        ComboFood comboFood = comboFoodRepository.findById(comboFoodId).orElseThrow(() -> new EntityNotFoundException("Combo not found"));

        booking.setUser(user);
        booking.setComboFood(comboFood);
        booking.setPrice(booking.getPrice() + comboFood.getPrice()); // Tính tổng giá mới

        return bookingRepository.save(booking);
    }

    public Booking saveBooking_Detail(String comboId, Long scheduleId, String payment) {
        Purchase purchase = purchaseService.Get();
        List<String> seatSymbols = new ArrayList<>();
        for (Purchase.Seat2 seat : purchase.getSeatsList()) {
            seatSymbols.add(seat.getSymbol());
        }

        Room room = roomRepository.findByName(purchase.getRoomName());
        List<Seat> seats = bookingService.getSeatsFromSymbolsAndRoom(seatSymbols, room);

        Schedule schedule = scheduleService.getScheduleById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid schedule Id"));

        Long comboFoodId = null;
        Long comboPrice = 0L;
        if (!comboId.equals("0-0")) {
            String[] comboDetails = comboId.split("-");
            comboFoodId = Long.parseLong(comboDetails[0]);
            comboPrice = Long.parseLong(comboDetails[1]);
        }

        Booking booking = new Booking();
        booking.setFilmName(purchase.getFilmTitle());
        booking.setPoster(purchase.getPoster());
        booking.setCinemaName(purchase.getCinemaName());
        booking.setCinemaAddress(purchase.getCinemaAddress());
        booking.setStartTime(parseDate(purchase.getStartTime()));
        booking.setSeatName(purchase.getSeats());
        booking.setRoomName(purchase.getRoomName());
        booking.setPayment(payment);
        booking.setStatus(true);
        booking.setCreateAt(new Date());
        booking.setPrice(purchase.getTotalPrice() + comboPrice);

        if (comboFoodId != null) {
            ComboFood comboFood = comboFoodService.getComboFoodById(comboFoodId)
                    .orElseThrow(() -> new EntityNotFoundException("Combo not found"));
            booking.setComboFood(comboFood);
        }

        User user = userService.getCurrentUser();
        booking.setUser(user);

        bookingService.saveBooking(booking, seats, schedule);

        return booking;
    }

    @Transactional
    public void saveBooking(Booking booking, List<Seat> seats, Schedule schedule) {
        bookingRepository.save(booking);
        for (Seat seat : seats) {
            BookingDetail bookingDetail = new BookingDetail();
            bookingDetail.setBooking(booking);
            bookingDetail.setSeat(seat);
            bookingDetail.setPrice(seat.getSeattype().getPrice());
            bookingDetail.setSchedule(schedule);
            bookingDetailRepository.save(bookingDetail);
        }
        //cập nhật trạng thái ghé đã đặt
        for (Seat seat : seats) {
            seat.setStatus("booked");
            seatRepository.save(seat);
        }
    }

    private Date parseDate(String dateStr) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return dateFormat.parse(dateStr);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

