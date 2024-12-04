package com.example.movietickets.demo.viewmodel;


import com.example.movietickets.demo.model.ComboFood;

public record ComboFoodVM(Long id, String name, String description, String poster, Long price) {
    public  static ComboFoodVM from(ComboFood c){
        return new ComboFoodVM(
                c.getId(),
                c.getComboName(),
                c.getDescription(),
                c.getPoster(),
                c.getPrice()
        );
    }
}
