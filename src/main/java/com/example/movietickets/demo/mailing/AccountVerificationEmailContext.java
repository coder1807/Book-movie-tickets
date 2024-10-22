package com.example.movietickets.demo.mailing;

import com.example.movietickets.demo.model.User;
import org.springframework.web.util.UriComponentsBuilder;

public class AccountVerificationEmailContext extends AbstractEmailContext {
    private String token;

    @Override
    public <T> void init(T context) {
        User user = (User) context;

        put("fullname", user.getFullname());
        setTemplateLocation("mailing/email-verification");
        setSubject("Hoàn tất đăng ký tài khoản của bạn!");
        setFrom("bhviet.910lhphong.cr@gmail.com");
        setTo(user.getEmail());
    }

    public void setToken(String token) {
        this.token = token;
        put("token", token);
    }

    public void buildVerificationUrl(final String baseURL, final String token) {
        final String url = UriComponentsBuilder.fromHttpUrl(baseURL)
                .path("/register/verify").queryParam("token", token).build().toUriString();
        put("verificationURL", url);
    }
}
