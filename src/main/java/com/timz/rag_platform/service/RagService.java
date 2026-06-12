package com.timz.rag_platform.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

@Service
public class RagService {

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.model}")
    private String groqModel;

    private final RestClient restClient;

    public RagService() {
        this.restClient = RestClient.builder()
            .baseUrl("https://api.groq.com/openai/v1")
            .build();
    }

    public String extraireTexte(String cheminFichier, String type) {
        try {
            if ("PDF".equals(type)) {
                return extraireTextePDF(cheminFichier);
            } else if ("EXCEL".equals(type)) {
                return extraireTexteExcel(cheminFichier);
            } else if ("IMAGE".equals(type)) {
                return "Contenu image : " + cheminFichier;
            }
        } catch (Exception e) {
            return "Erreur extraction : " + e.getMessage();
        }
        return "";
    }

    private String extraireTextePDF(String cheminFichier) throws Exception {
        try (PDDocument document = Loader.loadPDF(new File(cheminFichier))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extraireTexteExcel(String cheminFichier) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(cheminFichier);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                var sheet = workbook.getSheetAt(i);
                sb.append("Feuille: ").append(sheet.getSheetName()).append("\n");
                sheet.forEach(row -> {
                    row.forEach(cell -> {
                        sb.append(cell.toString()).append("\t");
                    });
                    sb.append("\n");
                });
            }
        }
        return sb.toString();
    }

    public String poserQuestion(String question, String contexte) {
        String prompt = "Tu es un assistant qui repond uniquement a partir des documents fournis.\n\n" +
            "Contexte des documents:\n" + contexte + "\n\n" +
            "Question: " + question + "\n\n" +
            "Reponds en francais de facon precise et cite tes sources.";

        try {
            Map<String, Object> requestBody = Map.of(
                "model", groqModel,
                "messages", List.of(
                    Map.of("role", "user", "content", prompt)
                ),
                "max_tokens", 1024,
                "temperature", 0.3
            );

            Map response = restClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + groqApiKey)
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(Map.class);

            if (response != null && response.containsKey("choices")) {
                List<Map> choices = (List<Map>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map message = (Map) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
        } catch (Exception e) {
            return "Erreur lors de la communication avec l'IA : " + e.getMessage();
        }
        return "Aucune reponse obtenue.";
    }
}