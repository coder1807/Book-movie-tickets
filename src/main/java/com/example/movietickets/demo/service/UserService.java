package com.example.movietickets.demo.service;

import com.example.movietickets.demo.Provider;
import com.example.movietickets.demo.Role;
import com.example.movietickets.demo.model.User;
import com.example.movietickets.demo.repository.IRoleRepository;
import com.example.movietickets.demo.repository.IUserRepository;
import com.example.movietickets.demo.repository.UserRepository;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional
public class UserService implements UserDetailsService {
    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IRoleRepository roleRepository;
    @Autowired
    private UserRepository user_Repository;

    public List<User> getAllUsers() {
        return userRepository.findAllByOrderByIdDesc();
    }

    public Optional<User> getUserById(Long id) {
        return user_Repository.findById((id));
    }

    public Long getCountUser() {
        return user_Repository.getCountUser();
    }

    // Lưu người dùng mới vào cơ sở dữ liệu sau khi mã hóa mật khẩu.
    public void save(@NotNull User user) {
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        userRepository.save(user);
    }

    public void setDefaultRole(String username) {
        userRepository.findByUsername(username).ifPresentOrElse(
                user -> {
                    user.getRoles().add(roleRepository.findRoleById(Role.USER.value));
                    user.setProvider("LOCAL");
                    userRepository.save(user);
                },
                () -> {
                    throw new UsernameNotFoundException("User not found");
                });
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getAuthorities());
    }

    // Tìm kiếm người dùng dựa trên tên đăng nhập.
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }

    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            // Xử lý trường hợp đăng nhập bằng tài khoản thông thường
            String username = ((UserDetails) principal).getUsername();
            return findByUsername(username).orElse(null);

        } else if (principal instanceof DefaultOidcUser) {
            // Xử lý trường hợp đăng nhập bằng Google (OIDC)
            DefaultOidcUser oidcUser = (DefaultOidcUser) principal;
            String email = oidcUser.getEmail();
            return findByEmail(email).orElse(null);

        } else if (principal instanceof DefaultOAuth2User) {
            // Xử lý trường hợp đăng nhập qua OAuth2 mà không sử dụng OIDC
            DefaultOAuth2User oauth2User = (DefaultOAuth2User) principal;
            String email = oauth2User.getAttribute("email");
            return findByEmail(email).orElse(null);

        } else {
            return null;
        }
    }

    public void saveOauthUser(String email, String username, String fullname, String provider) {
        if (username == null || userRepository.findByUsername(username).isPresent())
            return;
        var user = new User();
        user.setUsername(username);
        user.setFullname(fullname);
        user.setEmail(email != null ? email : username + "@example.com");
        user.setPassword(new BCryptPasswordEncoder().encode(username));
        user.setProvider(provider);
        user.getRoles().add(roleRepository.findRoleById(Role.USER.value));
        userRepository.save(user);
    }

    // public void saveOauthUser(String email, String username) {
    // if (userRepository.findByUsername(username).isPresent()) return;
    // var user = new User();
    // user.setUsername(username);
    // user.setEmail(email);
    // user.setPassword(new BCryptPasswordEncoder().encode(username));
    // user.setProvider(Provider.GOOGLE.value);
    // user.getRoles().add(roleRepository.findRoleById(Role.USER.value));
    // userRepository.save(user);
    // }

}