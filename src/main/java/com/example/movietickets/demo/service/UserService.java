package com.example.movietickets.demo.service;

import com.example.movietickets.demo.Provider;
import com.example.movietickets.demo.Role;
import com.example.movietickets.demo.exception.InvalidTokenException;
import com.example.movietickets.demo.exception.UserAlreadyExistException;
import com.example.movietickets.demo.mailing.AccountVerificationEmailContext;
import com.example.movietickets.demo.mailing.EmailService;
import com.example.movietickets.demo.model.SecureToken;
import com.example.movietickets.demo.model.User;
import com.example.movietickets.demo.repository.IRoleRepository;
import com.example.movietickets.demo.repository.IUserRepository;
import com.example.movietickets.demo.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@Transactional
public class UserService implements UserDetailsService {

    @Value("${site.base.url.https}")
    private String baseUrl;

    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private IRoleRepository roleRepository;
    @Autowired
    private UserRepository user_Repository;

    @Autowired
    private SecureTokenService secureTokenService;

    @Autowired
    private EmailService emailService;

    public List<User> getAllUsers() {
        return userRepository.findAllByOrderByIdDesc();
    }

    public Optional<User> getUserById(Long id) {
        return user_Repository.findById((id));
    }

    public Long getCountUser() {
        return user_Repository.getCountUser();
    }

    // Kiểm tra user tồn tại
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // Gửi thông báo xác nhận qua email bằng token
    public void sendRegistrationConfirmationEmail(User user) {
        SecureToken secureToken = secureTokenService.createToken();
        secureToken.setUser(user);
        secureTokenService.saveSecureToken(secureToken);

        AccountVerificationEmailContext context = new AccountVerificationEmailContext();
        context.init(user);
        context.setToken(secureToken.getToken());
        context.buildVerificationUrl(baseUrl, secureToken.getToken());

        try {
            emailService.sendMail(context);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    // Xác thực người dùng
    public boolean verifyUser(String token) throws InvalidTokenException {
        SecureToken secureToken = secureTokenService.findByToken(token);
        if (Objects.isNull(token) || !StringUtils.equals(token, secureToken.getToken()) || secureToken.isExpired()) {
            throw new InvalidTokenException("Token is not valid");
        }

        User user = userRepository.getById(secureToken.getUser().getId());
        if (Objects.isNull(user)) {
            return false;
        }

        // Sau khi xác thực thì set account_verified trong db lên 1 (true)
        user.setAccountVerified(true);
        userRepository.save(user);

        secureTokenService.removeToken(secureToken);
        return true;
    }

    // Lưu người dùng mới vào cơ sở dữ liệu sau khi mã hóa mật khẩu.
    public void save(@NotNull User user) throws UserAlreadyExistException {
        if (existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistException("This user already exist");
        }
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        userRepository.save(user);
        sendRegistrationConfirmationEmail(user);
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

        // Kiểm tra xem nếu người dùng đã xác thực tài khoản thì mới cho phép đăng nhập
        if (!user.isAccountVerified()) {
            throw new UsernameNotFoundException("Account is not verified");
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getAuthorities());
    }

    // Tìm kiếm người dùng dựa trên tên đăng nhập.
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }

    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return findByUsername(username).orElse(null);
        } else {
            return null;
        }
    }

    public void saveOauthUser(String email, String username, String fullname, String phoneNumber, String provider) {
        if (username == null || userRepository.findByUsername(username).isPresent())
            return;

        var user = new User();
        user.setUsername(username);
        user.setEmail(email != null ? email : username + "@example.com");
        user.setFullname(fullname);
        user.setPhone(phoneNumber);
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