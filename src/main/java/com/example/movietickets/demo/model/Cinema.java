package com.example.movietickets.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "Cinema")

public class Cinema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CINEMA_ID")
    private Long id;

    @Column(name = "CINEMA_NAME", nullable = false)
    private String name;

    @Column(name = "ADDRESS", nullable = false)
    private String address;

    @Column(name = "MAP", nullable = true)
    private String map;

    @OneToMany(mappedBy = "cinema")
    private List<Room> rooms;

}
