package com.example.movietickets.demo.API;

import com.example.movietickets.demo.DTO.UserDTO;
import com.example.movietickets.demo.model.User;
import com.example.movietickets.demo.repository.UserRepository;
import com.example.movietickets.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateUserInfo(
            @PathVariable Long id,
            @RequestBody UserDTO updateUserDTO) {
        try{
            User user = userRepository.findById(id).orElseThrow();
            if (updateUserDTO.getFullname() != null) {
                user.setFullname(updateUserDTO.getFullname());
            }
            if (updateUserDTO.getPhone() != null) {
                user.setPhone(updateUserDTO.getPhone());
            }
            if (updateUserDTO.getAddress() != null) {
                user.setAddress(updateUserDTO.getAddress());
            }
            if (updateUserDTO.getBirthday() != null){
                user.setBirthday(updateUserDTO.getBirthday());
            }

            userService.saveWithoutPass(user);
            UserDTO responseDTO = new UserDTO(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFullname(),
                    user.getPhone(),
                    user.getAddress(),
                    user.getBirthday()
            );
            return ResponseEntity.ok(new RestResponse("SUCCESS","Cập nhật thông tin thành công", Map.of("user", responseDTO)));
        }
        catch (Exception ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RestResponse("ERROR", "Đã xảy ra lỗi", Map.of("error", ex)));
        }
    }
}
