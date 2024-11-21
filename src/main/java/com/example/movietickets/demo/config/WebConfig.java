package com.example.movietickets.demo.config;

import com.example.movietickets.demo.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/login", "/register"); // Chỉ áp dụng cho URL /login
    }

    // Quản lý hiển thị ảnh động lập tức mà ko cần rerun project
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/assets/img/Card_Student_Image/**")
                .addResourceLocations("file:src/main/resources/static/assets/img/Card_Student_Image/")
                .setCachePeriod(0); // Đặt cache về 0 để luôn tải lại ảnh
    }
}