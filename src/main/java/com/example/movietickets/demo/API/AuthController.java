package com.example.movietickets.demo.API;

import com.example.movietickets.demo.DTO.LoginDTO;
import com.example.movietickets.demo.DTO.RegisterDTO;
import com.example.movietickets.demo.DTO.ResetPasswordDTO;
import com.example.movietickets.demo.DTO.UserDTO;
import com.example.movietickets.demo.config.GoogleConfig;
import com.example.movietickets.demo.exception.UserAlreadyExistException;
import com.example.movietickets.demo.mailing.DefaultEmailService;
import com.example.movietickets.demo.mailing.EmailService;
import com.example.movietickets.demo.model.SecureToken;
import com.example.movietickets.demo.model.User;
import com.example.movietickets.demo.repository.SecureTokenRepository;
import com.example.movietickets.demo.repository.UserRepository;
import com.example.movietickets.demo.service.UserService;
//import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
//import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
//import com.google.api.client.http.javanet.NetHttpTransport;
//import com.google.api.client.json.JsonFactory;
//import com.google.api.client.json.jackson2.JacksonFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(value = "/api", produces = "application/json; charset=UTF-8")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GoogleConfig googleConfig;
    @Autowired
    private SecureTokenRepository secureTokenRepository;
    @Autowired
    private DefaultEmailService emailService;


    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginDTO loginData) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginData.getUsername(), loginData.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = userService.findByUsername(loginData.getUsername()).orElseThrow();
            String type = userService.getUserType(user.getId());
            UserDTO userDTO = new UserDTO(user.getId(),user.getUsername(), user.getEmail(), user.getFullname(), user.getPhone(), user.getAddress(),user.getBirthday(),type);
            return ResponseEntity.ok(new RestResponse("SUCCESS", "Đăng nhập thành công", Map.of("user",userDTO)));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new RestResponse("ERROR", "Sai tài khoản hoặc mật khẩu", Map.of("error", ex)));
        }
        catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RestResponse("ERROR", "Đã xảy ra lỗi hệ thống! Vui lòng đăng nhập lại", Map.of("error", ex)));
        }
    }
    @PostMapping("register")
    public ResponseEntity<Object> register(@RequestBody RegisterDTO registerData){
        try{
            if (userService.existsByUsername(registerData.getUsername())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new RestResponse("ERROR", "Username đã tồn tại", null));
            }
            if (userService.existsByEmail(registerData.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new RestResponse("ERROR", "Email đã tồn tại", null));
            }
            User newUser = new User();
            newUser.setUsername(registerData.getUsername());
            newUser.setPassword(registerData.getPassword());
            newUser.setEmail(registerData.getEmail());
            newUser.setBirthday(registerData.getBirthday());
            userService.save(newUser);
            userService.setDefaultRole(newUser.getUsername());
            UserDTO userDTO = new UserDTO(newUser.getId(),newUser.getUsername(), newUser.getEmail(), newUser.getFullname(), newUser.getPhone(), newUser.getAddress(),newUser.getBirthday(),"STANDARD");

            return ResponseEntity.ok(new RestResponse("SUCCESS", "Bạn đã đăng ký tài khoản thành công! Hãy kiểm tra email để chúng tôi hoàn tất xác thực đăng ký tài khoản của bạn.",Map.of("user",userDTO)));
        } catch (UserAlreadyExistException e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<RestResponse> logout(HttpServletRequest request) {
        try {
            // Xóa session người dùng
            request.getSession().invalidate();
            return ResponseEntity.ok(new RestResponse("SUCCESS", "Đăng xuất thành công", null));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RestResponse("ERROR", "Đã xảy ra lỗi khi đăng xuất", null));
        }
    }
    @PostMapping("/save_oauth_user")
    public ResponseEntity<String> saveOauthUser(@RequestBody Map<String, Object> payload) {
        String email = (String) payload.get("email");
        String username = (String) payload.get("username");
        String fullname = (String) payload.get("fullname");
        String provider = (String) payload.get("provider");
        String dateOfBirthStr = (String) payload.get("dateOfBirth");
        LocalDate dateOfBirth = LocalDate.parse(dateOfBirthStr.substring(0, 10));

        // In thông tin người dùng và ngày sinh
        System.out.println("Email: " + email);
        System.out.println("Username: " + username);
        System.out.println("Fullname: " + fullname);
        System.out.println("Provider: " + provider);
        System.out.println("Date of Birth: " + dateOfBirth.toString());

        // Lưu thông tin vào cơ sở dữ liệu
        userService.saveOauthUser(email, username, fullname, provider, dateOfBirth);

        return ResponseEntity.ok("User saved successfully");
    }


//    @GetMapping("user/{email}")
//    public ResponseEntity<Object> fetchUserByEmail(@PathVariable String email){
//        User user = userService.findByEmail(email).orElseThrow();
//        String type = userService.getUserType(user.getId());
//        UserDTO userDTO = new UserDTO(user.getId(),user.getUsername(), user.getEmail(), user.getFullname(), user.getPhone(), user.getAddress(),user.getBirthday(),type);
//        return ResponseEntity.ok(new RestResponse("SUCCESS", "Tìm thấy user", Map.of("user",userDTO)));
//
//    }

    @GetMapping("user")
    public ResponseEntity<Object> fetchUserByEmail(@RequestParam Map<String, String> params){
        if (params.containsKey("email")){
            String email = params.get("email");
            User user = userService.findByEmail(email).orElseThrow();
            String type = userService.getUserType(user.getId());
            UserDTO userDTO = new UserDTO(user.getId(),user.getUsername(), user.getEmail(), user.getFullname(), user.getPhone(), user.getAddress(),user.getBirthday(),type);
            return ResponseEntity.ok(new RestResponse("SUCCESS", "Tìm thấy user", Map.of("user",userDTO)));
        }
        return ResponseEntity.badRequest().body("Invalid parameters");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        Optional<User> user = userService.findByEmail(email);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email không tồn tại!");
        }

        String token = UUID.randomUUID().toString();
        SecureToken resetToken = new SecureToken(token,LocalDateTime.now().plusMinutes(5), user.get());
        secureTokenRepository.save(resetToken);

        // Gửi email khôi phục
        emailService.sendResetPasswordEmail(user.get().getEmail(), token);

        return ResponseEntity.ok("Đã gửi email khôi phục mật khẩu!");
    }

    @GetMapping("/reset-password")
    public ResponseEntity<?> validateResetPasswordToken(@RequestParam("token") String token) {
        SecureToken secureToken = secureTokenRepository.findByToken(token);
        if (secureToken == null || secureToken.isExpired()) {
            return ResponseEntity.badRequest().body("Token không hợp lệ hoặc đã hết hạn.");
        }

        // Cập nhật URL để sử dụng intent filter hiện tại
        String resetPasswordUrl = "movieapp://com.example.movie_app/reset-password?token=" + token;

        return ResponseEntity.ok(resetPasswordUrl);
    }



    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDTO request) {
        SecureToken token = secureTokenRepository.findByToken(request.getToken());
        if (token == null || token.isExpired()) {
            return ResponseEntity.badRequest().body("Token không hợp lệ hoặc đã hết hạn!");
        }
        User user = token.getUser();
        user.setPassword(new BCryptPasswordEncoder().encode(request.getNewPassword()));
        userRepository.save(user);

        // Xoá hoặc đánh dấu token là đã sử dụng
        token.setExpiredAt(LocalDateTime.now());
        secureTokenRepository.save(token);
        return ResponseEntity.ok("Đặt lại mật khẩu thành công!");
    }

}
