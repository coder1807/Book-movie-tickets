package com.example.movietickets.demo.controller;

import com.example.movietickets.demo.mailing.DefaultEmailService;
import com.example.movietickets.demo.model.*;
import com.example.movietickets.demo.repository.RoomRepository;
import com.example.movietickets.demo.repository.SeatRepository;
import com.example.movietickets.demo.repository.UserRepository;
import com.example.movietickets.demo.service.*;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@AllArgsConstructor
@RequestMapping("/purchase")
public class PurchaseController {
    @Autowired
    private UserService userService;
    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScheduleServiceImpl scheduleService;

    @Autowired
    private ComboFoodService comboFoodService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SeatTypeService seatTypeService;

    @Autowired
    private DefaultEmailService emailService;

    @GetMapping
    public String showPurchase(Model model, @RequestParam(required = false) Long scheduleId) {
        if (purchaseService.IsExist()) {
            Purchase purchase = purchaseService.Get();
            System.out.println("scheduleId: " + scheduleId);
            System.out.println("selectedSeats: " + purchase.getSeats());
            model.addAttribute("selectedSeats", purchase.getSeats());
            model.addAttribute("filmTitle", purchase.getFilmTitle());
            model.addAttribute("category", purchase.getCategory());
            model.addAttribute("cinemaName", purchase.getCinemaName());
            model.addAttribute("cinemaAddress", purchase.getCinemaAddress());
            model.addAttribute("startTime", purchase.getStartTime());
            model.addAttribute("roomName", purchase.getRoomName());
            model.addAttribute("poster", purchase.getPoster());
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String formattedTotalPrice = currencyFormat.format(purchase.getTotalPrice());
            model.addAttribute("totalPrice", formattedTotalPrice);

            List<ComboFood> comboFoods = comboFoodService.getAllComboFood();
            Room room = roomRepository.findByName(purchase.getRoomName());
            List<Seat> seats = seatRepository.findByRoom(room);
            // add thêm trn header
            List<Category> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
            model.addAttribute("comboFoods", comboFoods);
            // lấy ra các seat booked
            model.addAttribute("purchase", purchase);
            model.addAttribute("seats", seats);
            model.addAttribute("scheduleId", scheduleId);

            List<SeatType> seatTypes = seatTypeService.getAllSeatTypes();
            List<String> formattedSeatPrice = seatTypes.stream()
                    .map(seatType -> currencyFormat.format(seatType.getPrice()))
                    .toList();

            model.addAttribute("seatTypes", seatTypes);
            model.addAttribute("seatPriceFormatted", formattedSeatPrice);
            User currentUser = userService.getCurrentUser();
            String userType = userService.getUserType(currentUser.getId());
            model.addAttribute("userType", userType);
        }
        return "/purchase/purchase";
    }

    @GetMapping("/clear")
    public String clearPurchase() {
        purchaseService.clearPurchase();
        return "redirect:/purchase";
    }

    @GetMapping("/email")
    public String email() {
        return "/mailing/paymentSuccess";
    }

    @PostMapping("/add")
    public String addPurchase(
            @RequestParam("seatSymbol") String seatSymbols,
            @RequestParam("totalPrice") Long totalPrice,
            @RequestParam("startTime") String startTime,
            @RequestParam("filmTitle") String filmTitle,
            @RequestParam("poster") String poster,
            @RequestParam("category") String category,
            @RequestParam("cinemaName") String cinemaName,
            @RequestParam("cinemaAddress") String cinemaAddress,
            @RequestParam("roomName") String roomName,
            @RequestParam("scheduleId") Long scheduleId,
            Model model) {
        System.out.println("scheduleId in addPurchase: " + scheduleId); // Debugging
        purchaseService.addToBuy(seatSymbols, filmTitle, poster, category, totalPrice, cinemaAddress, cinemaName,
                startTime, roomName);
        model.addAttribute("scheduleId", scheduleId);
        return "redirect:/purchase?scheduleId=" + scheduleId;
    }

    @GetMapping("/history")
    public String showPurchaseHistory(@RequestParam(required = false) Map<String, String> params, Model model) {
        // Kiểm tra nếu request có tham số từ VNPAY trả về
        if (params.containsKey("vnp_ResponseCode")) {
            String vnp_ResponseCode = params.get("vnp_ResponseCode");
            String vnp_TxnRef = params.get("vnp_TxnRef");
            System.out.println("vnp_ResponseCode: " + vnp_ResponseCode);
            // Xử lý kết quả từ VNPAY
            if ("00".equals(vnp_ResponseCode)) {
                // Thanh toán thành công
                model.addAttribute("paymentMessage", "Thanh toán thành công cho mã giao dịch: " + vnp_TxnRef);
            } else {
                // Thanh toán thất bại
                model.addAttribute("paymentMessage", "Thanh toán thất bại, mã lỗi: " + vnp_ResponseCode);
            }
        }

        User currentUser = userService.getCurrentUser();
        List<Booking> bookings = bookingService.getBookingsByUser(currentUser.getId()); // phương thức này để lấy các
        // booking của người dùng hiện
        // tại
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("bookings", bookings);
        return "/purchase/history";
    }

    @PostMapping("/checkout")
    public String checkout(
            @RequestParam("payment") String payment,
            @RequestParam String comboId, // nhận String từ form purrchase
            @RequestParam Long scheduleId,
            @RequestParam Long discountAmount,
            RedirectAttributes redirectAttributes) {
        if (purchaseService.IsExist()) {
            Purchase purchase = purchaseService.Get();

            // Tách comboId và comboPrice từ giá trị của request parameter
            Long comboFoodId = null;
            Long comboPrice = 0L;
            if (!comboId.equals("0-0")) { // Kiểm tra xem có chọn combo hay không
                String[] comboDetails = comboId.split("-");
                comboFoodId = Long.parseLong(comboDetails[0]);
                comboPrice = Long.parseLong(comboDetails[1]);
            }

            Booking booking = new Booking();
            booking.setPrice(purchase.getTotalPrice() + comboPrice - discountAmount); // cộng thêm giá từ food

            // Kiểm tra phương thức thanh toán
            if ("vnpay".equalsIgnoreCase(payment)) {
                return "redirect:/api/payment/create_payment?scheduleId=" + scheduleId + "&amount=" + booking.getPrice()
                        + "&comboId=" + comboId;
            }

            if ("paypal".equalsIgnoreCase(payment)) {
                return "redirect:/api/payment/create_paypal?scheduleId=" + scheduleId + "&amount=" + booking.getPrice()
                        + "&comboId=" + comboId;
            }

            if ("momo".equalsIgnoreCase(payment)) {
                return "redirect:/api/payment/create_momo?scheduleId=" + scheduleId + "&amount=" + booking.getPrice()
                        + "&comboId=" + comboId;
            }

            redirectAttributes.addFlashAttribute("message", "Đặt vé thành công!");
        } else {
            redirectAttributes.addFlashAttribute("message", "Không có thông tin đặt vé.");
        }
        return "redirect:/purchase/history"; // Chuyển hướng đến trang lịch sử mua vé
    }
}
