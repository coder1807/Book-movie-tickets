package com.example.movietickets.demo.controller;

import com.example.movietickets.demo.exception.UserAlreadyExistException;
import com.example.movietickets.demo.model.*;
import com.example.movietickets.demo.repository.UserRepository;
import com.example.movietickets.demo.service.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Year;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Controller
@AllArgsConstructor
public class ProfileController {
    @Autowired
    private final UserService userService;
    @Autowired
    private final CategoryService categoryService;
    private final BookingService bookingService;
    @Autowired
    private final CountryService countryService;
    @Autowired
    private CinemaService cinemaService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private GoogleMapsService googleMapsService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CardStudentService cardStudentService;


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public String home(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        List<Country> countries = countryService.getAllCountries();
        User currentUser = userService.getCurrentUser();
        Long userPoint = userService.getPointUser(currentUser.getId());
        String userType = userService.getUserType(currentUser.getId());

        Long totalPrice = bookingService.getTotalPriceByUser(currentUser.getId());
        Long bookingCount = bookingService.getCountBookingByUser(currentUser.getId());

        model.addAttribute("userPoint", userPoint);
        model.addAttribute("userType", userType);
        model.addAttribute("categories", categories);
        model.addAttribute("countries", countries);
        model.addAttribute("user", currentUser);
        model.addAttribute("bookingCount", bookingCount);
        model.addAttribute("totalPrice", totalPrice);
        // Gọi hàm tính tuổi và truyền vào model
        model.addAttribute("isStudentUni", getAge(currentUser));

        return "/profile/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam("full_name") String fullName,
                                @RequestParam("phone") String phone,
                                @RequestParam("address") String address,
                                Model model,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        User currentUser = userService.getCurrentUser();
        Long userPoint = userService.getPointUser(currentUser.getId());
        String userType = userService.getUserType(currentUser.getId());

        String oldFullname = currentUser.getFullname();
        String oldPhone = currentUser.getPhone();
        String oldAddress = currentUser.getAddress();

        String message = "Thay đổi thông tin thành công";
        if (!oldFullname.equals(fullName)) {
            currentUser.setFullname(fullName);
            message = message + "\nĐổi tên từ " + oldFullname + " thành " + fullName;
            session.setAttribute("fullname", fullName);
        }
        if (!oldPhone.equals(phone)) {
            currentUser.setPhone(phone);
            message = message + "\nĐổi SĐT từ " + oldPhone + " thành " + phone;
        }
        if (!oldAddress.equals(address)) {
            currentUser.setAddress(address);
            message = message + "\nĐổi địa chỉ từ " + oldAddress + " thành " + address;
        }
        redirectAttributes.addFlashAttribute("success", message);
        userService.saveWithoutPass(currentUser);

        model.addAttribute("userPoint", userPoint);
        model.addAttribute("userType", userType);
        model.addAttribute("user", currentUser);

        return "redirect:/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam("password") String oldPassword,
                                 @RequestParam("password2") String newPassword,
                                 @RequestParam("repassword2") String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentUser();
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(oldPassword, currentUser.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu cũ không chính xác!");
            return "redirect:/profile";
        }

        // Kiểm tra mật khẩu mới và xác nhận mật khẩu
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu mới và xác nhận mật khẩu không khớp!");
            return "redirect:/profile";
        }

        // Cập nhật mật khẩu mới cho người dùng
        currentUser.setPassword(newPassword);
        try {
            userService.save(currentUser);
        } catch (UserAlreadyExistException e) {
            throw new RuntimeException(e);
        }

        // Thêm thông báo thành công
        redirectAttributes.addFlashAttribute("success", "Thay đổi mật khẩu thành công!");
        return "redirect:/profile";

    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my-points")
    public String showPoints(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        List<Country> countries = countryService.getAllCountries();
        User currentUser = userService.getCurrentUser();
        Long userPoint = userService.getPointUser(currentUser.getId());
        String userType = userService.getUserType(currentUser.getId());

        Long totalPrice = bookingService.getTotalPriceByUser(currentUser.getId());
        Long bookingCount = bookingService.getCountBookingByUser(currentUser.getId());
        int progressWidth = Math.round(userPoint * 100 / 5000f);
        String nextType;
        if (userType.equals("STANDARD")) {
            nextType = "FRIEND";
        } else {
            nextType = "VIP";
        }
        model.addAttribute("categories", categories);
        model.addAttribute("countries", countries);
        model.addAttribute("user", currentUser);
        model.addAttribute("userPoint", userPoint);
        model.addAttribute("userType", userType);
        model.addAttribute("nextType", nextType);
        model.addAttribute("bookingCount", bookingCount);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("progressWidth", progressWidth);
        return "/profile/points";
    }

    @GetMapping("/profile/nearest")
    public String getNearestCinemas(Model model) {
        User currentUser = userService.getCurrentUser();
        List<Cinema> cinemas = cinemaService.getAllCinemas();
        List<CinemaDistance> cinemaDistances = googleMapsService.getDistances(currentUser.getAddress(), cinemas);
        model.addAttribute("cinemaDistances", cinemaDistances);
        return "/profile/nearest";
    }

    @GetMapping("/profile/detectStudent/{userId}")
    public String getInfoStudentUni(@PathVariable("userId") Long userId, Model model) {
        User currentUser = userService.getCurrentUser();
        // Kiểm tra id người dùng hiện tại với userId của url
        if (currentUser == null || !currentUser.getId().equals(userId)) {
            return "redirect:/error/404";
        }
        // Kiểm tra xem nếu tuổi không phù hợp
        if (!getAge(currentUser)) {
            return "redirect:/error/404";
        }
        model.addAttribute("userId", userId);
        model.addAttribute("cardstudent", new CardStudent());
        return "/Profile/detect-card-uni";
    }

//    @PostMapping("/profile/saveStudentData/{userId}")
//    public String saveStudentData(@PathVariable("userId") Long userId, @ModelAttribute CardStudent cardStudent) {
//        User currentUser = userService.getCurrentUser();
//        if (currentUser == null || !currentUser.getId().equals(userId)) {
//            return "redirect:/error/404";
//        }
//        cardStudent.setUserId(currentUser);
//        cardStudent.setCreatedDate(LocalDate.now());
//        try {
//            cardStudentService.save(cardStudent);
//            return "redirect:/profile";
//        } catch (Exception e) {
//            return "redirect:/error/404";
//        }
//    }

    @PostMapping("/profile/saveStudentData/{userId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveStudentData(@PathVariable("userId") Long userId,
                                                               @RequestPart("imageCard") MultipartFile imageCard) {
        User currentUser = userService.getCurrentUser();
        Map<String, Object> response = new HashMap<>();

        // Kiểm tra người dùng hợp lệ
        if (currentUser == null || !currentUser.getId().equals(userId)) {
            response.put("success", false);
            response.put("error", "Người dùng không hợp lệ.");
            return ResponseEntity.badRequest().body(response);
        }

        // Thiết lập thông tin cho CardStudent
//        cardStudent.setUserId(currentUser);
//        cardStudent.setCreatedDate(LocalDate.now());
//        cardStudent.setVerified(true);

        try {
            // Lưu ảnh vào thư mục và lấy tên ảnh
//            String imageName = saveImage(imageCard);
//            cardStudent.setImageCard(imageName); // Lưu tên ảnh vào CardStudent

            // Lưu CardStudent vào cơ sở dữ liệu
//            cardStudentService.save(cardStudent);

            // Đặt timeout phòng khi mạng yếu
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            okhttp3.RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("img", imageCard.getOriginalFilename(),
                            okhttp3.RequestBody.create(MediaType.parse(imageCard.getContentType()),
                                    imageCard.getBytes()))
                    .build();
            Request request = new Request.Builder()
                    .url("https://card-ai-api.phatnef.me/predict")
                    .method("POST", body)
                    .build();
            Response res = client.newCall(request).execute();
//            assert res.body() != null;
//            System.out.println(res.body().string());

            // xu ly thong tin xem anh hop le khong
            //...


            // hop le
//            String imageName = saveImage(imageCard);
//            cardStudent.setImageCard(imageName); // Lưu tên ảnh vào CardStudent

            // Chuyển đổi JSON phản hồi từ API thành JsonNode
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResponse = objectMapper.readTree(res.body().string());

            String cardValue = jsonResponse.get("card").get("value").asText();
            String ngaysinhValue = jsonResponse.get("ngaysinh").get("value").asText();
            String hotenValue = jsonResponse.get("hoten").get("value").asText();
            String mssvValue = jsonResponse.get("mssv").get("value").asText();
            String khoahocValue = jsonResponse.get("khoahoc").get("value").asText();

            // Lưu vào cơ sở dữ liệu
            CardStudent cardStudent = new CardStudent();
            cardStudent.setUserId(currentUser);
            cardStudent.setSchoolName(cardValue);
            cardStudent.setBirthDay(ngaysinhValue);
            cardStudent.setFullName(hotenValue);
            cardStudent.setStudentId(mssvValue);
            cardStudent.setSchoolYear(khoahocValue);
            cardStudent.setVerified(true);
            cardStudent.setCreatedDate(LocalDate.now());
            String imageName = saveImage(imageCard);
            cardStudent.setImageCard(imageName);
            cardStudentService.save(cardStudent);

            // Trả về thông báo thành công
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Nếu có lỗi xảy ra
            System.out.println("Loi: " + e.toString());
            response.put("success", false);
            response.put("error", "Có lỗi xảy ra khi lưu dữ liệu.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private String saveImage(MultipartFile imageCard) throws IOException {
        if (imageCard.isEmpty()) {
            throw new IOException("Không có ảnh nào được chọn");
        }

        // Lưu ảnh và trả về tên file ảnh
        File uploadDir = new File("D:\\Python Project\\image_recognition\\img");
        if (!uploadDir.exists()) {
            uploadDir.mkdir();
        }

        String fileName = UUID.randomUUID().toString() + "_" + imageCard.getOriginalFilename();
        File file = new File(uploadDir + "\\" + fileName);
        imageCard.transferTo(file);

        return fileName;
    }

    private boolean getAge(User currentUser) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Optional<User> user = userRepository.findByUsername(authentication.getName());
            if (user.isPresent()) {
                int currentYear = Year.now().getValue(); // Lấy năm hiện tại
                int birthYear = user.get().getBirthday().getYear(); // Lấy năm sinh của user
                int age = currentYear - birthYear;
                return age >= 18 && age <= 22;
            }
        }
        return false;
    }

}

