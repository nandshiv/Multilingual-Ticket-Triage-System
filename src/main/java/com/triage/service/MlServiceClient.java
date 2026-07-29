package com.triage.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Service
public class MlServiceClient {

    private final RestTemplate restTemplate;
    private final String mlServiceUrl;

    public MlServiceClient(
            @Value("${ml.service.url:http://localhost:8000}") String mlServiceUrl) {
        this.restTemplate = new RestTemplate();
        this.mlServiceUrl = mlServiceUrl;
    }

    public TranslationResult translate(String text) {
        String url = mlServiceUrl + "/translate";
        Map<String, String> request = Map.of("text", text);
        try {
            return restTemplate.postForObject(url, request, TranslationResult.class);
        } catch (Exception e) {
            // Fallback
            TranslationResult res = new TranslationResult();
            res.setTranslated_text(text);
            res.setDetected_language("en");
            return res;
        }
    }

    public ClassificationResult classify(String text) {
        String url = mlServiceUrl + "/classify";
        Map<String, String> request = Map.of("text", text);
        try {
            return restTemplate.postForObject(url, request, ClassificationResult.class);
        } catch (Exception e) {
            ClassificationResult res = new ClassificationResult();
            res.setCategory("Unclassified");
            res.setConfidence(0.0);
            return res;
        }
    }

    public List<Double> embed(String text) {
        String url = mlServiceUrl + "/embed";
        Map<String, String> request = Map.of("text", text);
        try {
            EmbeddingResult result = restTemplate.postForObject(url, request, EmbeddingResult.class);
            return result != null ? result.getEmbedding() : null;
        } catch (Exception e) {
            return null;
        }
    }

    // DTOs matching the FastAPI Python output
    public static class TranslationResult {
        private String translated_text;
        private String detected_language;
        public String getTranslated_text() { return translated_text; }
        public void setTranslated_text(String translated_text) { this.translated_text = translated_text; }
        public String getDetected_language() { return detected_language; }
        public void setDetected_language(String detected_language) { this.detected_language = detected_language; }
    }

    public static class ClassificationResult {
        private String category;
        private Double confidence;
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public Double getConfidence() { return confidence; }
        public void setConfidence(Double confidence) { this.confidence = confidence; }
    }

    public static class EmbeddingResult {
        private List<Double> embedding;
        public List<Double> getEmbedding() { return embedding; }
        public void setEmbedding(List<Double> embedding) { this.embedding = embedding; }
    }
}
