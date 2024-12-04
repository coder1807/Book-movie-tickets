package com.example.movietickets.demo.API;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestResponse {
    private String status; // SUCCESS hoặc ERROR
    private String message; // Mô tả ngắn gọn
    private Object data;    // Dữ liệu trả về (nếu có)
    public RestResponse(String status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
