package com.example.movietickets.demo.controller;

import com.example.movietickets.demo.exception.UserAlreadyExistException;
import com.example.movietickets.demo.model.*;
import com.example.movietickets.demo.repository.CardStudentRepository;
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
    @Autowired
    private CardStudentRepository cardStudentRepository;



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
        model.addAttribute("isStudentUni", userService.getAge(currentUser));

        // Nếu user hiện tại đã xác thực ảnh thẻ
        if (cardStudentRepository.isVerified(currentUser.getId()) != null) {
            model.addAttribute("isVerified", true);
            Optional<Long> cardStudentIdOpt = cardStudentService.getCardStudentByUserId(currentUser.getId());
            if (cardStudentIdOpt.isPresent()) {
                Long cardStudentId = cardStudentIdOpt.get();
                // Lấy CardStudent từ cardStudentId
                CardStudent cardStudent = cardStudentService.getCardStudentById(cardStudentId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid Card Student Id:" + cardStudentId));
                model.addAttribute("hrefUrl", "/profile/showDataStudent/" + cardStudent.getId());
            }
            if (cardStudentIdOpt.isEmpty()) {
                return "redirect:error/404";
            }
        }

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
        String oldAddress = "";
        if (currentUser.getAddress() != null){
            oldAddress = currentUser.getAddress();
        };

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
        currentUser.setPassword(new BCryptPasswordEncoder().encode(newPassword));
        userRepository.save(currentUser);

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

    // Hàm nhận diện thẻ sinh viên
    @GetMapping("/profile/detectStudent/{userId}")
    public String getInfoStudentUni(@PathVariable("userId") Long userId, Model model) {
        User currentUser = userService.getCurrentUser();
        // Kiểm tra id người dùng hiện tại với userId của url
        if (currentUser == null || !currentUser.getId().equals(userId)) {
            return "redirect:/error/404";
        }
        // Kiểm tra xem nếu tuổi của user không phù hợp thì trả về lỗi
        if (!userService.getAge(currentUser)) {
            return "redirect:/error/404";
        }
        // Nếu user hện tại đã xác thực thẻ sinh viên nhưng vẫn truy cập vào trang detect thì trả về lỗi
        Boolean isVerified = cardStudentRepository.isVerified(currentUser.getId());
        if (isVerified != null && isVerified) {
            return "redirect:/error/404";
        }
        model.addAttribute("userId", userId);
        model.addAttribute("cardstudent", new CardStudent());
        return "/profile/detect-card-uni";
    }

    // Hàm hiển thị dữ liệu thẻ sinh viên của người dùng đã xác thực
    @GetMapping("/profile/showDataStudent/{cardStudentId}")
    public String showDataStudentUni(Model model) {
        User currentUser = userService.getCurrentUser();
        Optional<Long> cardStudentIdOpt = cardStudentService.getCardStudentByUserId(currentUser.getId());
        Long cardStudentId = cardStudentIdOpt.get();
        // Lấy CardStudent từ cardStudentId
        CardStudent cardStudent = cardStudentService.getCardStudentById(cardStudentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Card Student Id:" + cardStudentId));
        model.addAttribute("cardstudent", cardStudent);

        // Kiểm tra ảnh đã tồn tại chưa
        Boolean hasImage = cardStudent.getImageCard() != null;
        model.addAttribute("hasImage", hasImage);

        // Hiển thị ảnh
        String imagePath = "/assets/img/Card_Student_Image/" + cardStudent.getImageCard();
        model.addAttribute("imagePath", hasImage ? imagePath : null);

        // Hiển thị các thông tin xác thực và ẩn button ko cần thiết
        model.addAttribute("isVerified", cardStudent.isVerified());
        model.addAttribute("school", cardStudent.getSchoolName().equals("hutech") ? "Đại học Công Nghệ TP. HCM" : "Đại học Văn Lang");
        return "/profile/detect-card-uni";
    }

    // Hàm lưu thông tin từ thẻ sinh viên vào db
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

        try {
            // Đặt timeout gọi API phòng khi mạng yếu
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

            // Chuyển đổi JSON phản hồi từ API thành JsonNode
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResponse = objectMapper.readTree(res.body().string());

            String cardValue = jsonResponse.has("card") && jsonResponse.get("card").has("value")
                    ? jsonResponse.get("card").get("value").asText()
                    : null;

            String mssvValue = jsonResponse.has("mssv") && jsonResponse.get("mssv").has("value")
                    ? jsonResponse.get("mssv").get("value").asText()
                    : null;

            // Kiểm tra các giá trị bắt buộc
            if (cardValue == null || cardValue.isEmpty()) {
                throw new IllegalArgumentException("Thông tin trường học không được để trống.");
            }
            if (mssvValue == null || mssvValue.isEmpty()) {
                throw new IllegalArgumentException("Thông tin mssv không được để trống.");
            }

            // được phép null
            String ngaysinhValue = jsonResponse.has("ngaysinh") && jsonResponse.get("ngaysinh").has("value")
                    ? jsonResponse.get("ngaysinh").get("value").asText()
                    : null;

            String hotenValue = jsonResponse.has("hoten") && jsonResponse.get("hoten").has("value")
                    ? jsonResponse.get("hoten").get("value").asText()
                    : null;

            String khoahocValue = jsonResponse.has("khoahoc") && jsonResponse.get("khoahoc").has("value")
                    ? jsonResponse.get("khoahoc").get("value").asText()
                    : null;

            // Kiểm tra thông tin thẻ sinh viên đã tồn tại trong db chưa
            Optional<CardStudent> existingCard = cardStudentService.findByCardValueAndMssv(cardValue, mssvValue);
            if (existingCard.isPresent()) {
                response.put("success", false);
                response.put("error", "Thẻ sinh viên này đã tồn tại trong hệ thống. Vui lòng cập nhật ảnh thẻ sinh viên của bạn !!!");
                return ResponseEntity.badRequest().body(response);
            }

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

        // Lấy đường dẫn thực tế đến thư mục trong dự án
        String uploadDirPath = new File("src/main/resources/static/assets/img/Card_Student_Image").getAbsolutePath();
        File uploadDir = new File(uploadDirPath);

        // Tạo thư mục nếu chưa tồn tại
        if (!uploadDir.exists()) {
            if (!uploadDir.mkdirs()) {
                throw new IOException("Không thể tạo thư mục lưu trữ ảnh tại: " + uploadDirPath);
            }
        }

        String fileName = UUID.randomUUID().toString() + "_" + imageCard.getOriginalFilename();
        File file = new File(uploadDir + "\\" + fileName);
        imageCard.transferTo(file);

        return fileName;
    }

}

