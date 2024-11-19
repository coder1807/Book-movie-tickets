package com.example.movietickets.demo.service;

import com.example.movietickets.demo.model.CardStudent;
import com.example.movietickets.demo.repository.CardStudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CardStudentService {
    @Autowired
    private CardStudentRepository cardStudentRepository;

    public CardStudent save(CardStudent cardStudent) {
        return cardStudentRepository.save(cardStudent);
    }
}
