package com.example.movietickets.demo.API;

import com.example.movietickets.demo.DTO.LoginDTO;
import com.example.movietickets.demo.DTO.RegisterDTO;
import com.example.movietickets.demo.DTO.UserDTO;
import com.example.movietickets.demo.exception.UserAlreadyExistException;
import com.example.movietickets.demo.model.User;
import com.example.movietickets.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = "/api", produces = "application/json; charset=UTF-8")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

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
            UserDTO userDTO = new UserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getFullname(), user.getPhone(), user.getAddress());
            return ResponseEntity.ok(new RestResponse("SUCCESS", "Đăng nhập thành công", Map.of("user", userDTO)));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new RestResponse("ERROR", "Sai tài khoản hoặc mật khẩu", Map.of("error", ex)));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RestResponse("ERROR", "Đã xảy ra lỗi hệ thống! Vui lòng đăng nhập lại", Map.of("error", ex)));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody RegisterDTO registerData) {
        try {
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
            userService.save(newUser);
            userService.setDefaultRole(newUser.getUsername());
            UserDTO userDTO = new UserDTO(newUser.getId(), newUser.getUsername(), newUser.getEmail(), newUser.getFullname(), newUser.getPhone(), newUser.getAddress());

            return ResponseEntity.ok(new RestResponse("SUCCESS", "Bạn đã đăng ký tài khoản thành công! Hãy kiểm tra email để chúng tôi hoàn tất xác thực đăng ký tài khoản của bạn.", Map.of("user", userDTO)));
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
}
