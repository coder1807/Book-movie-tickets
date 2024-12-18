package com.example.movietickets.demo.DTO;

import lombok.Data;

@Data

public class SeatDTO {
    private Long id;
    private String symbol;

    // Constructor
    public SeatDTO(Long id, String symbol) {
        this.id = id;
        this.symbol = symbol;
    }
}
