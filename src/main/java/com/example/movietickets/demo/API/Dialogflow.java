package com.example.movietickets.demo.API;

import com.example.movietickets.demo.service.CategoryService;
import com.example.movietickets.demo.service.FilmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class Dialogflow {
    @Autowired
    private CategoryService categoryService; // Đảm bảo rằng CategoryService chứa phương thức getSuggestedCategories

    @Autowired
    private FilmService filmService;

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> handleWebhook(@RequestBody Map<String, Object> request) {
        try {
            // Lấy Intent từ payload một cách an toàn
            Map<String, Object> queryResult = (Map<String, Object>) request.get("queryResult");
            if (queryResult == null) {
                return createErrorResponse("Invalid request format: missing queryResult");
            }

            Map<String, Object> intent = (Map<String, Object>) queryResult.get("intent");
            if (intent == null) {
                return createErrorResponse("Invalid request format: missing intent");
            }

            String intentName = (String) intent.get("displayName");
            if (intentName == null) {
                return createErrorResponse("Invalid request format: missing intent displayName");
            }

            // Xử lý các intent
            switch (intentName.toLowerCase()) {
                case "gợi ý thể loại":
                    try {
//                        String responseText = categoryService.getSuggestedCategories();
                        Map<String, Object> responseText = categoryService.getSuggestedCategories();

                        return new ResponseEntity<>(responseText, HttpStatus.OK);
                    } catch (Exception e) {
                        return createErrorResponse("Error while getting category suggestions: " + e.getMessage());
                    }
                case "gợi ý phim":
                    try {
                        Map<String, Object> responseText = filmService.getSuggestedMovies();
                        return new ResponseEntity<>(responseText, HttpStatus.OK);
                    } catch (Exception e) {
                        return createErrorResponse("Error while getting movie suggestions: " + e.getMessage());
                    }
                default:
                    return createErrorResponse("Xin lỗi, tôi không hiểu câu hỏi.");
            }

        } catch (Exception e) {
            return createErrorResponse("Internal server error: " + e.getMessage());
        }
    }

    private ResponseEntity<Map<String, Object>> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("fulfillmentText", message);
        return ResponseEntity.ok(response);
    }


    private ResponseEntity<Map<String, Object>> createErrorResponse(String errorMessage) {
        Map<String, Object> response = new HashMap<>();
        response.put("fulfillmentText", "Đã xảy ra lỗi: " + errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
