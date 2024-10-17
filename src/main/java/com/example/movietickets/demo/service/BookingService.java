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

    public Long getCountBookingByUser(long user_id) {return bookingRepository.getCountBookingByUser(user_id);}

    public Long getTotalPriceByUser(long user_id) {return bookingRepository.getTotalPriceByUser(user_id);}
    public List<Seat> getSeatsFromSymbolsAndRoom(List<String> seatSymbols, Room room) {
        return seatRepository.findBySymbolInAndRoom(seatSymbols, room);
    }

    public List<Booking> getAllBookings() {
        // Thay đổi phương thức để lấy các booking của người dùng hiện tại
        return bookingRepository.findAll();
    }
    public List<Booking> getBookingsByUser(long user_id){return bookingRepository.findByUser(user_id);}

//    public List<Booking> getBookingsByCurrentUser() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication instanceof OAuth2AuthenticationToken) { //log in voi Oauth2
//            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
//            String email = null;
//
//            Object principal = oauthToken.getPrincipal();
//            if (principal instanceof DefaultOidcUser) {
//                email = ((DefaultOidcUser) principal).getEmail();
//            } else if (principal instanceof DefaultOAuth2User) {
//                email = ((DefaultOAuth2User) principal).getAttribute("email");
//            }
//
//            if (email != null) {
//                User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy User"));
//                return bookingRepository.findByUser(user);
//            }
//        } else if (authentication instanceof UsernamePasswordAuthenticationToken) { //login binh thuong
//            String username = authentication.getName();
//            User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy User"));
//            return bookingRepository.findByUser(user);
//        }
//
//        throw new UsernameNotFoundException("Không tìm thấy User");
//    }


//    public List<Booking> getBookingsByCurrentUser() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String username = authentication.getName();
//        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy User"));
//        return bookingRepository.findByUser(user);
//    }

    public Booking saveBookingWithCombo(Long userId, Long comboFoodId, Booking booking) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        ComboFood comboFood = comboFoodRepository.findById(comboFoodId).orElseThrow(() -> new EntityNotFoundException("Combo not found"));

        booking.setUser(user);
        booking.setComboFood(comboFood);
        booking.setPrice(booking.getPrice() + comboFood.getPrice()); // Tính tổng giá mới

        return bookingRepository.save(booking);
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
}

