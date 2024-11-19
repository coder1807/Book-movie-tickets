package com.example.movietickets.demo.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "Card_Student")
public class CardStudent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_student_id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "USER_ID", nullable = false)
    private User userId;

    @Column(name = "image_card", nullable = false)
    private String imageCard;

    @Column(name = "school_name", nullable = false)
    private String schoolName;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "isVerified", nullable = false)
    private boolean isVerified;

    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate;

    @Column(name = "fullname")
    private String fullName;

    @Column(name = "birthday")
    private String birthDay;

    @Column(name = "school_year")
    private String schoolYear;
}
