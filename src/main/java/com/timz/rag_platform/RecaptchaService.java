package com.timz.rag_platform;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.Map;

@Service
public class RecaptchaService {

    @Value("${recaptcha.secret-key}")
    private String secretKey;

    private final RestClient restClient;

    public RecaptchaService() {
        this.restClient = RestClient.builder()
            .baseUrl("https://www.google.com/recaptcha/api")
            .build();
    }

    public boolean validerToken(String token) {
        if (token == null || token.isBlank()) return false;
        try {
            Map response = restClient.post()
                .uri("/siteverify?secret=" + secretKey + "&response=" + token)
                .retrieve()
                .body(Map.class);
            return response != null && Boolean.TRUE.equals(response.get("success"));
        } catch (Exception e) {
            return false;
        }
    }
}