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
import com.google.gson.JsonParser;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.web.bind.annotation.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.servlet.view.RedirectView;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
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

import static org.apache.http.client.utils.DateUtils.parseDate;

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
    private PaypalService paypalService;

    @Autowired
    private DefaultEmailService emailService;

    @Autowired
    private UserService userService;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("create_payment")
    public ResponseEntity<?> createPayment(@RequestParam("amount") long amount, @RequestParam("scheduleId") Long scheduleId, @RequestParam("comboId") String comboId, HttpServletRequest request) throws UnsupportedEncodingException {

        // get current hostname from proxy spring boot (tùy biến localhost và ngrok hostname)
        String homeURL = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();

        String amountValue = String.valueOf(amount * 100);
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
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_ReturnUrl", homeURL + Config.vnp_ReturnUrl);

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

        // Trả về trang HTML tự động chuyển hướng
        String htmlResponse = "<html><body>" + "<form id='paymentForm' action='" + paymentUrl + "' method='GET'></form>" + "<script type='text/javascript'>document.getElementById('paymentForm').submit();</script>" + "</body></html>";

        HttpSession session = (HttpSession) request.getSession();
        session.setAttribute("comboId", comboId);
        session.setAttribute("scheduleId", scheduleId);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(paymentUrl));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("create_paypal")
    public ResponseEntity<?> createPaymentPaypal(@RequestParam("amount") long amount, @RequestParam("scheduleId") Long scheduleId, @RequestParam("comboId") String comboId) throws UnsupportedEncodingException, PayPalRESTException {
        Payment payment = paypalService.createPayment(amount, "USD", "paypal", "sale", "Payment Description", scheduleId, "http://localhost:8080/api/payment/paypal_success");
        for (Links links : payment.getLinks()) {
            if (links.getRel().equals("approval_url")) {
                HttpHeaders headers = new HttpHeaders();
                headers.setLocation(URI.create(links.getHref()));
                return new ResponseEntity<>(headers, HttpStatus.FOUND);
            }
        }
        return ResponseEntity.ok(payment);
    }


    @GetMapping("create_momo")
    public ResponseEntity<?> createPaymentMoMo(@RequestParam("amount") Long amount, @RequestParam("scheduleId") Long scheduleId, @RequestParam("comboId") String comboId,@RequestParam(value = "isMobile", defaultValue = "false") boolean isMobile, HttpSession session) throws Exception {
        String amountStr = String.valueOf(amount);
        String orderId = MoMoConfig.PARTNER_CODE + new Date().getTime();
        session.setAttribute("orderId", orderId); // doi sang get params
        session.setAttribute("scheduleId", scheduleId);
        session.setAttribute("comboId", comboId);
        // save booking & orderId to db
        /// ...

        // get current hostname from proxy spring boot (tùy biến localhost và ngrok hostname)
        String homeURL = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();

        String rawSignature = String.format("accessKey=%s&amount=%s&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s", MoMoConfig.ACCESS_KEY, amount, MoMoConfig.EXTRA_DATA, homeURL + MoMoConfig.IPN_URL, orderId, MoMoConfig.ORDER_INFO, MoMoConfig.PARTNER_CODE, homeURL + MoMoConfig.REDIRECT_URL + "?transaction_id=" + orderId, orderId, MoMoConfig.REQUEST_TYPE);

        String signature = MoMoConfig.hmacSHA256(rawSignature, MoMoConfig.SECRET_KEY);

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("partnerCode", MoMoConfig.PARTNER_CODE);
        requestBody.addProperty("requestId", orderId);
        requestBody.addProperty("amount", amountStr);
        requestBody.addProperty("orderId", orderId);
        requestBody.addProperty("orderInfo", MoMoConfig.ORDER_INFO);
        requestBody.addProperty("redirectUrl", homeURL + MoMoConfig.REDIRECT_URL + "?transaction_id=" + orderId);
        requestBody.addProperty("ipnUrl", homeURL + MoMoConfig.IPN_URL);
        requestBody.addProperty("lang", MoMoConfig.LANG);
        requestBody.addProperty("requestType", MoMoConfig.REQUEST_TYPE);
        requestBody.addProperty("autoCapture", MoMoConfig.AUTO_CAPTURE);
        requestBody.addProperty("extraData", MoMoConfig.EXTRA_DATA);
        requestBody.addProperty("signature", signature);

        String result = MoMoConfig.sendHttpPost("https://test-payment.momo.vn/v2/gateway/api/create", requestBody.toString());

        JsonObject responseJson = new Gson().fromJson(result, JsonObject.class);

        if (responseJson.has("payUrl")) {
            String payUrl = responseJson.get("payUrl").getAsString();
            if (isMobile) {
                // Tạo JsonObject riêng biệt
                JsonObject response = new JsonObject();
                response.addProperty("payUrl", payUrl);
                return ResponseEntity.ok(response.toString());
            }
            else {
                // Redirect cho web
                HttpHeaders headers = new HttpHeaders();
                headers.setLocation(URI.create(payUrl));
                return new ResponseEntity<>(headers, HttpStatus.FOUND);
            }

        }

        return ResponseEntity.ok(responseJson);
    }


    @GetMapping("/paypal_success")
    public RedirectView handlePaypalSuccess(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            Payment payment = paypalService.executePayment(paymentId, payerId);
            if ("approved".equals(payment.getState())) {
                String comboId = (String) session.getAttribute("comboId");
                Long scheduleId = (Long) session.getAttribute("scheduleId");
                User currentUser = userService.getCurrentUser();
                Booking savedBooking = bookingService.saveBooking_Detail(comboId, scheduleId, "PAYPAL");
                emailService.sendSimpleMessage(currentUser.getEmail(), currentUser, savedBooking);
                redirectAttributes.addFlashAttribute("success", "Thanh toán thành công!");
                return new RedirectView("/purchase/history");
            }
        } catch (PayPalRESTException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi trong quá trình xử lý thanh toán PayPal.");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        return new RedirectView("/purchase/history");
    }

    // callback IPN Url for momo QR code
    @PostMapping("/handlePayment")
    public String queryTransaction(HttpSession session) throws Exception {
        String orderId = (String) session.getAttribute("orderId"); // Lấy orderId từ getMapping /api/payment/handlePayment

        String rawSignature = String.format("accessKey=%s&orderId=%s&partnerCode=%s&requestId=%s", MoMoConfig.ACCESS_KEY, orderId, MoMoConfig.PARTNER_CODE, orderId);

        String signature = MoMoConfig.hmacSHA256(rawSignature, MoMoConfig.SECRET_KEY);

        // URL endpoint của MoMo
        String url = "https://test-payment.momo.vn/v2/gateway/api/query";

        // Tạo body request
        String requestBody = String.format("{\n" + "  \"partnerCode\": \"%s\",\n" + "  \"requestId\": \"%s\",\n" + "  \"orderId\": \"%s\",\n" + "  \"lang\": \"%s\",\n" + "  \"signature\": \"%s\"\n" + "}", MoMoConfig.PARTNER_CODE, orderId, orderId, MoMoConfig.LANG, signature);

        // Thiết lập headers (có thể không cần thiết tùy vào yêu cầu)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Tạo HttpEntity với body và headers
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        // Gửi POST request
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        return response.getBody();
    }

    // ...?...&json=true
    @GetMapping("/handlePayment")
    public RedirectView handlePayment(@RequestParam Map<String, String> requestParams, HttpServletRequest request, RedirectAttributes redirectAttributes) throws Exception {
        String homeURL = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
        String transaction_id = requestParams.get("transaction_id"); // mã giao dịch momoId
//        ...

        User currentUser = userService.getCurrentUser();
        Booking savedBooking;
        HttpSession session = request.getSession();
        String comboId = (String) session.getAttribute("comboId");
        Long scheduleId = (Long) session.getAttribute("scheduleId");
        if (scheduleId == null) {
            System.out.println("handlePayment: Thông tin giao dịch không hợp lệ.");
            redirectAttributes.addFlashAttribute("error", "Thông tin giao dịch không hợp lệ.");
            return new RedirectView(homeURL + "/purchase/history");
        }
        String vnp_ResponseCode = requestParams.get("vnp_ResponseCode");
        String momo_resultCode = requestParams.get("resultCode");
        if ("00".equals(vnp_ResponseCode)) {
            savedBooking = bookingService.saveBooking_Detail(comboId, scheduleId, "VNPAY");
            try {
                emailService.sendSimpleMessage(currentUser.getEmail(), currentUser, savedBooking);
                return new RedirectView(homeURL + "/purchase/history");
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        }

        if (transaction_id != null && !transaction_id.isEmpty()) {
            if ("0".equals(momo_resultCode)) {
                savedBooking = bookingService.saveBooking_Detail(comboId, scheduleId, "MOMO");
                try {
                    emailService.sendSimpleMessage(currentUser.getEmail(), currentUser, savedBooking);

                    // Kiểm tra User-Agent để xác định có phải mobile không
                    String userAgent = request.getHeader("User-Agent");
                    if (userAgent != null && (userAgent.contains("Mobile") || userAgent.contains("Android") || userAgent.contains("iPhone"))) {
                        // Nếu là mobile, trả về app link
                        return new RedirectView("movieapp://com.example.movie_app/");
                    } else {
                        // Nếu không phải mobile, trả về trang lịch sử
                        return new RedirectView(homeURL + "/purchase/history");
                    }
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            } else {
                savedBooking = bookingService.saveBooking_Detail(comboId, scheduleId, "MOMO");
                try {
                    emailService.sendSimpleMessage(currentUser.getEmail(), currentUser, savedBooking);

                    // Kiểm tra User-Agent để xác định có phải mobile không
                    String userAgent = request.getHeader("User-Agent");
                    if (userAgent != null && (userAgent.contains("Mobile") || userAgent.contains("Android") || userAgent.contains("iPhone"))) {
                        // Nếu là mobile, trả về app link
                        return new RedirectView("movieapp://com.example.movie_app/");
                    } else {
                        // Nếu không phải mobile, trả về trang lịch sử
                        return new RedirectView(homeURL + "/purchase/history");
                    }
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return new RedirectView(homeURL + "/purchase/history");
    }

    private String loadHtmlTemplate(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }
}