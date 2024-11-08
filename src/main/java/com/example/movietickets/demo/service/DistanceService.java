package com.example.movietickets.demo.service;

import com.example.movietickets.demo.model.Cinema;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.List;

@Service
public class DistanceService {

    @Value("${google.maps.api.key}")
    private String apiKey;

    private final String DISTANCE_MATRIX_URL = "https://maps.googleapis.com/maps/api/distancematrix/json";

    public String findNearestCinema(String userAddress, List<Cinema> cinemas) {
        RestTemplate restTemplate = new RestTemplate();
        String nearestCinema = null;
        int shortestDistance = Integer.MAX_VALUE;

        for (Cinema cinema : cinemas) {
            String url = DISTANCE_MATRIX_URL + "?origins=" + userAddress +
                    "&destinations=" + cinema.getAddress() + "&key=" + apiKey;
            String response = restTemplate.getForObject(url, String.class);

            JSONObject jsonObject = new JSONObject(response);
            int distance = jsonObject.getJSONArray("rows").getJSONObject(0)
                    .getJSONArray("elements").getJSONObject(0)
                    .getJSONObject("distance").getInt("value");

            if (distance < shortestDistance) {
                shortestDistance = distance;
                nearestCinema = cinema.getName();
            }
        }

        return nearestCinema;
    }
}