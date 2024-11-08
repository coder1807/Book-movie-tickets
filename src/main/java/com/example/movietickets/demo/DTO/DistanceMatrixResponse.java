package com.example.movietickets.demo.DTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class DistanceMatrixResponse {
    @Setter
    @Getter
    private List<Row> rows;

    public static class Row {
        @Setter
        @Getter
        private List<Element> elements;

    }

    public static class Element {
        @Setter
        @Getter
        private Distance distance;

    }

    public static class Distance {
        @Setter
        @Getter
        private String text;

        @Setter
        @Getter
        private int value; // Distance in meters

    }
}
