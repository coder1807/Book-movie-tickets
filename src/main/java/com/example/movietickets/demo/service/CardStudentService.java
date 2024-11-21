package com.example.movietickets.demo.service;

import com.example.movietickets.demo.model.CardStudent;
import com.example.movietickets.demo.repository.CardStudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import javax.smartcardio.Card;
import java.util.Optional;

@Service
public class CardStudentService {
    @Autowired
    private CardStudentRepository cardStudentRepository;

    public CardStudent save(CardStudent cardStudent) {
        return cardStudentRepository.save(cardStudent);
    }

    // Lấy CardStudent theo ID
    public Optional<CardStudent> getCardStudentById(Long id) {
        return cardStudentRepository.findById(id);
    }

    // Lấy CardStudent theo userId
    public Optional<Long> getCardStudentByUserId(Long userId) {
        return cardStudentRepository.findIdByUserId(userId);
    }
}
