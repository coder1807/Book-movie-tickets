package com.example.movietickets.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "SeatType")
public class SeatType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SEAT_TYPE_ID")
    private Long id;

    private String type;

    @Min(value = 1, message = "Giá phải lớn hơn 0")
    @Column(name = "PRICE")
    private Long price;

    @OneToMany(mappedBy = "seattype", cascade = CascadeType.ALL, orphanRemoval = true)
    //cascade=all cho phép khi Update tat cả bảng con tham chiếu den SeatType,orphanRemoval=true cho phep khi xoa seatType thi tat ca bang con cx bi xoa theo
    private List<Seat> seats;
}
