package com.example.movietickets.demo.viewmodel;

import com.example.movietickets.demo.model.Category;

public record CategoryVM(Long id, String name) {
    public  static CategoryVM from(Category c){
        return new CategoryVM(
                c.getId(),
                c.getName()
        );
    }
}
