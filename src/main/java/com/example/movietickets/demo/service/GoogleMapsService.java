package com.example.movietickets.demo.service;
import com.example.movietickets.demo.DTO.DistanceMatrixResponse;
import com.example.movietickets.demo.model.Cinema;
import com.example.movietickets.demo.model.CinemaDistance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoogleMapsService {

    @Value("${google.maps.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<CinemaDistance> getDistances(String userAddress, List<Cinema> cinemas) {
        String origins = userAddress;
        String destinations = cinemas.stream()
                .map(Cinema::getAddress)
                .collect(Collectors.joining("|"));

        String url = UriComponentsBuilder.fromHttpUrl("https://maps.googleapis.com/maps/api/distancematrix/json")
                .queryParam("origins", origins)
                .queryParam("destinations", destinations)
                .queryParam("key", apiKey)
                .toUriString();

        // Log the URL to ensure it's being built correctly
        System.out.println("Request URL: " + url);

        // Make the API call
        DistanceMatrixResponse response = restTemplate.getForObject(url, DistanceMatrixResponse.class);

        // Log the response
        System.out.println("Distance Matrix API Response: " + response);

        return parseResponse(response, cinemas);
    }


    private List<CinemaDistance> parseResponse(DistanceMatrixResponse response, List<Cinema> cinemas) {
        List<CinemaDistance> cinemaDistances = new ArrayList<>();

        // Check if the response is valid
        if (response != null && response.getRows() != null && !response.getRows().isEmpty()) {
            List<DistanceMatrixResponse.Element> elements = response.getRows().get(0).getElements();
            if (elements != null && elements.size() == cinemas.size()) {
                for (int i = 0; i < elements.size(); i++) {
                    int distanceValue = elements.get(i).getDistance().getValue();
                    CinemaDistance cinemaDistance = new CinemaDistance(cinemas.get(i), distanceValue);
                    cinemaDistances.add(cinemaDistance);
                }
                cinemaDistances.sort(Comparator.comparingInt(CinemaDistance::getDistance));
            } else {
                System.out.println("Error: Mismatch in cinema count and response elements count");
            }
        } else {
            System.out.println("Error: No rows in response or response is null");
        }

        return cinemaDistances;
    }

}
