package com.example.movietickets.demo.controller;

import com.example.movietickets.demo.mailing.AbstractEmailContext;
import com.example.movietickets.demo.mailing.DefaultEmailService;
import com.example.movietickets.demo.mailing.EmailService;
import com.example.movietickets.demo.DTO.PaymentResDTO;
import com.example.movietickets.demo.config.Config;
import com.example.movietickets.demo.config.MoMoConfig;
import com.example.movietickets.demo.model.*;
import com.example.movietickets.demo.repository.RoomRepository;
import com.example.movietickets.demo.repository.SeatRepository;
import com.example.movietickets.demo.repository.UserRepository;
import com.example.movietickets.demo.service.*;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.web.bind.annotation.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

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
    private  PaypalService paypalService;

    @Autowired
    private DefaultEmailService emailService;

    @GetMapping("create_payment")
    public ResponseEntity<?> createPayment(@RequestParam("amount") long amount, @RequestParam("scheduleId") Long scheduleId, @RequestParam("comboId") String comboId) throws UnsupportedEncodingException {

        // Kiểm tra giá trị amount
        System.out.println("Amount received: " + amount);

        //String orderType = "other";
        //long amount = Integer.parseInt(req.getParameter("amount"))*100;
        //String bankCode = req.getParameter("bankCode");

        //long amount = 10000;
        String amountValue = String.valueOf(amount*100);
        String vnp_TxnRef = Config.getRandomNumber(8);
        //String vnp_IpAddr = Config.getIpAddress(req);
        String vnp_TmnCode = Config.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", Config.vnp_Version);
        vnp_Params.put("vnp_Command", Config.vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", amountValue);
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_BankCode", "NCB");
        vnp_Params.put("vnp_Locale", "vn");
        //vnp_Params.put("vnp_TransactionNo", "NCB");
        vnp_Params.put("vnp_IpAddr", Config.vnp_IpAddr);

        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", "other" );
        vnp_Params.put("vnp_ReturnUrl", Config.vnp_ReturnUrl);


        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 5);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = Config.hmacSHA512(Config.secretKey, hashData.toString());
        vnp_Params.put("vnp_SecureHash", vnp_SecureHash);
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = Config.vnp_PayUrl + "?" + queryUrl;

        PaymentResDTO paymentResDTO = new PaymentResDTO();
        paymentResDTO.setStatus("OK");
        paymentResDTO.setMessage("Successfully");
        paymentResDTO.setUrl(paymentUrl);

        Purchase purchase = purchaseService.Get();
        List<String> seatSymbols = new ArrayList<>();
        for (Purchase.Seat2 seat : purchase.getSeatsList()) {
            seatSymbols.add(seat.getSymbol());
        }

        Room room = roomRepository.findByName(purchase.getRoomName());
        List<Seat> seats = bookingService.getSeatsFromSymbolsAndRoom(seatSymbols, room);

        // Lấy schedule từ scheduleId
        Schedule schedule = scheduleService.getScheduleById(scheduleId).orElseThrow(() -> new IllegalArgumentException("Invalid schedule Id"));
            // Tách comboId và comboPrice từ giá trị của request parameter
            Long comboFoodId = null;
            Long comboPrice = 0L;
            if (!comboId.equals("0-0")) { // Kiểm tra xem có chọn combo hay không
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
        booking.setPayment("vnpay");
        booking.setStatus(true); // Hoặc giá trị khác tùy vào logic của bạn
        booking.setCreateAt(new Date());
        booking.setPrice(purchase.getTotalPrice()+ comboPrice); //cộng thêm giá từ food

        if (comboFoodId != null) {
            ComboFood comboFood = comboFoodService.getComboFoodById(comboFoodId).orElseThrow(() -> new EntityNotFoundException("Combo not found"));
            booking.setComboFood(comboFood);
        }


        // Lấy thông tin người dùng hiện tại
        // Lấy thông tin người dùng hiện tại
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = getUserFromAuthentication(authentication);
        booking.setUser(user);


        bookingService.saveBooking(booking, seats, schedule);

        // Trả về trang HTML tự động chuyển hướng
        String htmlResponse = "<html><body>"
                + "<form id='paymentForm' action='" + paymentUrl + "' method='GET'></form>"
                + "<script type='text/javascript'>document.getElementById('paymentForm').submit();</script>"
                + "</body></html>";

        // return ResponseEntity.status(HttpStatus.OK).body(htmlResponse);

        //return ResponseEntity.status(HttpStatus.OK).body(paymentResDTO);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(paymentUrl));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);

        //return paymentUrl;

    }

    @GetMapping("create_paypal")
    public ResponseEntity<?> createPaymentPaypal(@RequestParam("amount") long amount, @RequestParam("scheduleId") Long scheduleId, @RequestParam("comboId") String comboId) throws UnsupportedEncodingException, PayPalRESTException {


        Payment payment =  paypalService.createPayment(amount,"USD", "paypal", "sale", "Payment Description", scheduleId, "http://localhost:8080/purchase/history");

        Purchase purchase = purchaseService.Get();
        List<String> seatSymbols = new ArrayList<>();
        for (Purchase.Seat2 seat : purchase.getSeatsList()) {
            seatSymbols.add(seat.getSymbol());
        }

        Room room = roomRepository.findByName(purchase.getRoomName());
        List<Seat> seats = bookingService.getSeatsFromSymbolsAndRoom(seatSymbols, room);

        // Lấy schedule từ scheduleId
        Schedule schedule = scheduleService.getScheduleById(scheduleId).orElseThrow(() -> new IllegalArgumentException("Invalid schedule Id"));
        // Tách comboId và comboPrice từ giá trị của request parameter
        Long comboFoodId = null;
        Long comboPrice = 0L;
        if (!comboId.equals("0-0")) { // Kiểm tra xem có chọn combo hay không
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
        booking.setPayment("paypal");
        booking.setStatus(true); // Hoặc giá trị khác tùy vào logic của bạn
        booking.setCreateAt(new Date());
        booking.setPrice(purchase.getTotalPrice()+ comboPrice); //cộng thêm giá từ food

        if (comboFoodId != null) {
            ComboFood comboFood = comboFoodService.getComboFoodById(comboFoodId).orElseThrow(() -> new EntityNotFoundException("Combo not found"));
            booking.setComboFood(comboFood);
        }


        // Lấy thông tin người dùng hiện tại
        // Lấy thông tin người dùng hiện tại
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = getUserFromAuthentication(authentication);
        booking.setUser(user);


        bookingService.saveBooking(booking, seats, schedule);

        for(Links links: payment.getLinks()){
                    if(links.getRel().equals("approval_url")) {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setLocation(URI.create(links.getHref()));
                        return new ResponseEntity<>(headers, HttpStatus.FOUND);
                    }
        }

        return ResponseEntity.ok(payment);

    }
    @GetMapping("create_momo")
    public ResponseEntity<?> createPaymentMoMo(@RequestParam("amount") Long amount, @RequestParam("scheduleId") Long scheduleId, @RequestParam("comboId") String comboId) throws Exception {
        System.out.println(amount);
        System.out.println(scheduleId);
        System.out.println(comboId);
        String amountStr = String.valueOf(amount);
        String orderId = MoMoConfig.PARTNER_CODE + new Date().getTime();
        String requestId = orderId;

        String rawSignature = String.format(
                "accessKey=%s&amount=%s&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
                MoMoConfig.ACCESS_KEY, amount, MoMoConfig.EXTRA_DATA, MoMoConfig.IPN_URL, orderId, MoMoConfig.ORDER_INFO, MoMoConfig.PARTNER_CODE, MoMoConfig.REDIRECT_URL, requestId, MoMoConfig.REQUEST_TYPE
        );

        String signature = MoMoConfig.hmacSHA256(rawSignature, MoMoConfig.SECRET_KEY);


        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("partnerCode", MoMoConfig.PARTNER_CODE);
        requestBody.addProperty("requestId", requestId);
        requestBody.addProperty("amount", amountStr);
        requestBody.addProperty("orderId", orderId);
        requestBody.addProperty("orderInfo", MoMoConfig.ORDER_INFO);
        requestBody.addProperty("redirectUrl", MoMoConfig.REDIRECT_URL);
        requestBody.addProperty("ipnUrl", MoMoConfig.IPN_URL);
        requestBody.addProperty("lang", MoMoConfig.LANG);
        requestBody.addProperty("requestType", MoMoConfig.REQUEST_TYPE);
        requestBody.addProperty("autoCapture", MoMoConfig.AUTO_CAPTURE);
        requestBody.addProperty("extraData", MoMoConfig.EXTRA_DATA);
        requestBody.addProperty("signature", signature);

        String result = MoMoConfig.sendHttpPost("https://test-payment.momo.vn/v2/gateway/api/create", requestBody.toString());
        JsonObject responseJson = new Gson().fromJson(result, JsonObject.class);

        Purchase purchase = purchaseService.Get();
        List<String> seatSymbols = new ArrayList<>();
        for (Purchase.Seat2 seat : purchase.getSeatsList()) {
            seatSymbols.add(seat.getSymbol());
        }

        Room room = roomRepository.findByName(purchase.getRoomName());
        List<Seat> seats = bookingService.getSeatsFromSymbolsAndRoom(seatSymbols, room);


        Schedule schedule = scheduleService.getScheduleById(scheduleId).orElseThrow(() -> new IllegalArgumentException("Invalid schedule Id"));


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
        booking.setPayment("momo");
        booking.setStatus(true);
        booking.setCreateAt(new Date());
        booking.setPrice(purchase.getTotalPrice() + comboPrice);

        if (comboFoodId != null) {
            ComboFood comboFood = comboFoodService.getComboFoodById(comboFoodId).orElseThrow(() -> new EntityNotFoundException("Combo not found"));
            booking.setComboFood(comboFood);
        }


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = getUserFromAuthentication(authentication);
        booking.setUser(user);

         String userMail = user.getEmail();

        bookingService.saveBooking(booking, seats, schedule);


        if (responseJson.has("payUrl")) {
            String payUrl = responseJson.get("payUrl").getAsString();
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(payUrl));
            String userEmail = user.getEmail();
            scheduleEmailSending(() -> {
                try {
                    emailService.sendSimpleMessage(userEmail, user, booking);
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            }, 30);
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }
        return ResponseEntity.ok(responseJson);
    }

    @GetMapping("/callback")
    public ResponseEntity<?> handleCallback(@RequestBody Map<String, Object> body) {
        System.out.println("MoMo callback data: " + body);
        int resultCode = (int) body.get("resultCode");
        if (resultCode == 0) {
            return ResponseEntity.ok(body);
        } else {
            return ResponseEntity.status(400).body("Transaction failed with resultCode: " + resultCode);
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
    private User getUserFromAuthentication(Authentication authentication) {
        if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            // Trường hợp đăng nhập thông thường
            String username = ((org.springframework.security.core.userdetails.User) authentication.getPrincipal()).getUsername();
            return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        } else if (authentication.getPrincipal() instanceof DefaultOAuth2User) {
            // Trường hợp đăng nhập bằng OAuth2 (Facebook, Google)
            String email = ((DefaultOAuth2User) authentication.getPrincipal()).getAttribute("email");
            return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        }
        throw new UsernameNotFoundException("User not found");
    }
    public void scheduleEmailSending(Runnable task, long delayInSeconds) {
        scheduler.schedule(task, delayInSeconds, TimeUnit.SECONDS);
    }

    private String loadHtmlTemplate(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }
}